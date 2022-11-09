package io.intino.ness.datahubterminalplugin.terminal;

import com.google.gson.Gson;
import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datalake.Split;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.datahub.model.Event;
import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Terminal;
import io.intino.ness.datahubterminalplugin.*;
import io.intino.ness.datahubterminalplugin.MavenTerminalExecutor.Target;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.PluginLauncher.Phase.*;

public class TerminalPublisher {
	private final File root;
	private final Terminal terminal;
	private final Configuration conf;
	private final Map<String, String> versions;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final boolean includeMaster;
	private final List<Tank.Event> tanks;

	public TerminalPublisher(File root, Terminal terminal, List<Tank.Event> tanks, Configuration configuration, Map<String, String> versions, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger, PluginLauncher.Notifier notifier, boolean includeMaster) {
		this.root = root;
		this.terminal = terminal;
		this.tanks = tanks;
		this.conf = configuration;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		this.notifier = notifier;
		this.includeMaster = includeMaster;
	}

	public boolean publish() {
		try {
			if (!checkPublish() || !createSources()) return false;
			logger.println("Publishing " + terminal.name$() + "...");
			new MavenTerminalExecutor(root, basePackage, includeMaster ? Target.EventsAndMaster : Target.Events, terminalNameArtifact(), versions, conf, systemProperties, logger).mvn(invokedPhase == INSTALL ? "install" : "deploy");
			logger.println("Terminal " + terminal.name$() + " published!");
			return true;
		} catch (Throwable e) {
			logger.println(e.getMessage() == null ? e.toString() : e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private boolean checkPublish() {
		try {
			Version version = new Version(conf.artifact().version());
			if (!version.isSnapshot() && (invokedPhase == DISTRIBUTE || invokedPhase == DEPLOY))
				return false;
		} catch (IntinoException e) {
			return false;
		}
		return true;
	}

	private boolean createSources() {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		Map<Event, Datalake.Split> eventSplitMap = collectEvents(tanks);
		if (duplicatedEvents()) return false;
		new TerminalRenderer(terminal, eventSplitMap, srcDirectory, basePackage).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
		return true;
	}

	private boolean duplicatedEvents() {
		final Set<String> duplicatedPublish = terminal.publish() != null ? findDuplicates(terminal.publish().tanks().stream().map(Tank.Event::qn).collect(Collectors.toList())) : Collections.emptySet();
		final Set<String> duplicatedSubscribe = terminal.subscribe() != null ? findDuplicates(terminal.subscribe().tanks().stream().map(Tank.Event::qn).collect(Collectors.toList())) : Collections.emptySet();
		if (!duplicatedPublish.isEmpty()) {
			logger.println("Duplicated publishing event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			notifier.notifyError("Duplicated publishing event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			return true;
		}
		if (!duplicatedSubscribe.isEmpty()) {
			logger.println("Duplicated subscription event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			notifier.notifyError("Duplicated subscription event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			return true;
		}
		return false;
	}

	public Set<String> findDuplicates(List<String> listContainingDuplicates) {
		final Set<String> set = new HashSet<>();
		return listContainingDuplicates.stream().filter(yourInt -> !set.add(yourInt)).collect(Collectors.toSet());
	}

	private void writeManifest(File srcDirectory) {
		List<String> publish = terminal.publish() != null ? terminal.publish().tanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().tanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), eventSplits());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String namespace(Event event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn() + "." : "";
	}

	private String terminalNameArtifact() {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private Map<String, Set<String>> eventSplits() {
		Map<String, Set<String>> eventSplits = terminal.publish() == null ? new HashMap<>() : eventSplitOf(terminal.publish().tanks());
		if (terminal.subscribe() == null) return eventSplits;
		Map<String, Set<String>> subscribeEventSplits = eventSplitOf(terminal.subscribe().tanks());
		for (String eventType : subscribeEventSplits.keySet()) {
			if (!eventSplits.containsKey(eventType)) eventSplits.put(eventType, new HashSet<>());
			eventSplits.get(eventType).addAll(subscribeEventSplits.get(eventType));
		}
		return eventSplits;
	}

	private Map<String, Set<String>> eventSplitOf(List<Tank.Event> tanks) {
		return tanks.stream().
				collect(Collectors.toMap(this::eventQn,
						tank -> tank.asTank().isSplitted() ? tank.asTank().asSplitted().split().leafs().stream().map(Split::qn).collect(Collectors.toSet()) : Collections.emptySet(), (a, b) -> b));
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.bpm() != null) {
			Split split = terminal.bpm().split();
			String statusQn = terminal.bpm().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			tankClasses.put((split != null ? split.qn() + "." : "") + statusClassName, statusQn);
		}
		return tankClasses;
	}

	private String eventQn(Tank.Event t) {
		return namespace(t.event()) + t.event().name$();
	}

	private Map<Event, Split> collectEvents(List<Tank.Event> tanks) {
		Map<Event, Split> events = new HashMap<>();
		for (Tank.Event tank : tanks) {
			List<Event> hierarchy = hierarchy(tank.event());
			Split split = tank.asTank().isSplitted() ? tank.asTank().asSplitted().split() : null;
			events.put(hierarchy.get(0), split);
			hierarchy.remove(0);
			hierarchy.forEach(e -> events.put(e, null));
		}
		return events;
	}

	private List<Event> hierarchy(Event event) {
		Set<Event> events = new LinkedHashSet<>();
		events.add(event);
		if (event.isExtensionOf()) events.addAll(hierarchy(event.asExtensionOf().parent()));
		return new ArrayList<>(events);
	}
}