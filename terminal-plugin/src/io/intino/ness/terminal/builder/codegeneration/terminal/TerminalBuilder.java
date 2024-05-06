package io.intino.ness.terminal.builder.codegeneration.terminal;

import com.google.gson.Gson;
import io.intino.alexandria.logger.Logger;
import io.intino.builder.CompilerConfiguration;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.datahub.model.Event;
import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Terminal;
import io.intino.magritte.framework.Layer;
import io.intino.ness.terminal.builder.IntinoException;
import io.intino.ness.terminal.builder.Manifest;
import io.intino.ness.terminal.builder.codegeneration.Formatters;
import io.intino.ness.terminal.builder.codegeneration.Project;
import io.intino.ness.terminal.builder.codegeneration.master.MasterRenderer;
import io.intino.ness.terminal.builder.util.ErrorUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;
import static io.intino.ness.terminal.builder.Formatters.firstUpperCase;
import static io.intino.ness.terminal.builder.Formatters.snakeCaseToCamelCase;

public class TerminalBuilder {
	private final File root;
	private final Terminal terminal;
	private final CompilerConfiguration configuration;
	private final Map<String, String> versions;
	private final String basePackage;

	public TerminalBuilder(File root, Terminal terminal, CompilerConfiguration configuration, Map<String, String> versions) {
		this.root = root;
		this.terminal = terminal;
		this.configuration = configuration;
		this.versions = versions;
		this.basePackage = configuration.groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifactId()).toString().toLowerCase();
	}

	public Project build() throws IntinoException {
		try {
			createSources();
			configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Terminal " + terminal.name$() + " created!");
			PomRenderer pomRenderer = new PomRenderer(configuration, versions, root, basePackage);
			return new Project(pomRenderer.coors(terminal), pomRenderer.render(terminal));
		} catch (IntinoException e) {
			throw e;
		} catch (Throwable e) {
			throw new IntinoException("Error rendering terminal: " + ErrorUtils.getMessage(e));
		}
	}

	private void createSources() throws IntinoException {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		Map<Event, Datalake.Split> eventSplitMap = collectEvents();
		checkDuplicatedEvents();
		if (!terminal.graph().entityList().isEmpty())
			new MasterRenderer(srcDirectory, terminal.graph(), configuration, basePackage).renderTerminal(terminal);
		new TerminalRenderer(terminal, eventSplitMap, srcDirectory, basePackage).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
	}

	private void checkDuplicatedEvents() throws IntinoException {
		final Set<String> duplicatedPublish = terminal.publish() != null ? findDuplicates(terminal.publish().eventTanks().stream().map(Tank.Event::qn).collect(Collectors.toList())) : Collections.emptySet();
		final Set<String> duplicatedSubscribe = terminal.subscribe() != null ? findDuplicates(terminal.subscribe().eventTanks().stream().map(Tank.Event::qn).collect(Collectors.toList())) : Collections.emptySet();
		if (!duplicatedPublish.isEmpty()) {
			configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Duplicated publishing event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			throw new IntinoException("Duplicated publishing event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));

		}
		if (!duplicatedSubscribe.isEmpty()) {
			configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Duplicated subscription event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
			throw new IntinoException("Duplicated subscription event in terminal " + terminal.name$() + ": " + String.join(", ", duplicatedPublish));
		}
	}

	public Set<String> findDuplicates(List<String> listContainingDuplicates) {
		final Set<String> set = new HashSet<>();
		return listContainingDuplicates.stream().filter(yourInt -> !set.add(yourInt)).collect(Collectors.toSet());
	}


	private void writeManifest(File srcDirectory) {
		List<String> publish = terminal.publish() != null ? terminal.publish().eventTanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().eventTanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + firstUpperCase(snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), eventSplits());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().eventTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().eventTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.bpm() != null) {
			Datalake.Split split = terminal.bpm().split();
			String statusQn = terminal.bpm().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			tankClasses.put((split != null ? split.qn() + "." : "") + statusClassName, statusQn);
		}
		return tankClasses;
	}

	private String eventQn(Tank.Event t) {
		return namespace(t.event()) + t.event().name$();
	}

	private String namespace(Layer event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn() + "." : "";
	}

	private Map<Event, Datalake.Split> collectEvents() {
		List<Tank.Event> tanks = tanks(terminal);
		Map<Event, Datalake.Split> events = new HashMap<>();
		for (Tank.Event tank : tanks) {
			List<Event> hierarchy = hierarchy(tank.event());
			Datalake.Split split = tank.asTank().isSplitted() ? tank.asTank().asSplitted().split() : null;
			events.put(hierarchy.get(0), split);
			hierarchy.remove(0);
			hierarchy.forEach(e -> events.put(e, null));
		}
		return events;
	}


	private Map<String, Set<String>> eventSplits() {
		Map<String, Set<String>> eventSplits = terminal.publish() == null ? new HashMap<>() : eventSplitOf(terminal.publish().eventTanks());
		if (terminal.subscribe() == null) return eventSplits;
		Map<String, Set<String>> subscribeEventSplits = eventSplitOf(terminal.subscribe().eventTanks());
		for (String eventType : subscribeEventSplits.keySet()) {
			if (!eventSplits.containsKey(eventType)) eventSplits.put(eventType, new HashSet<>());
			eventSplits.get(eventType).addAll(subscribeEventSplits.get(eventType));
		}
		return eventSplits;
	}


	private Map<String, Set<String>> eventSplitOf(List<Tank.Event> tanks) {
		return tanks.stream().
				collect(Collectors.toMap(this::eventQn,
						tank -> tank.asTank().isSplitted() ? tank.asTank().asSplitted().split().leafs().stream().map(Datalake.Split::qn).collect(Collectors.toSet()) : Collections.emptySet(), (a, b) -> b));
	}

	private List<Tank.Event> tanks(Terminal terminal) {
		List<Tank.Event> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().eventTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().eventTanks());
		return tanks;
	}

	private List<Event> hierarchy(Event event) {
		Set<Event> events = new LinkedHashSet<>();
		events.add(event);
		if (event.isExtensionOf()) events.addAll(hierarchy(event.asExtensionOf().parent()));
		return new ArrayList<>(events);
	}
}