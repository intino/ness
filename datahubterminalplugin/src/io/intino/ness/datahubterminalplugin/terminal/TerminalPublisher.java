package io.intino.ness.datahubterminalplugin.terminal;

import com.google.gson.Gson;
import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.datahub.model.Message;
import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Terminal;
import io.intino.ness.datahubterminalplugin.*;
import io.intino.ness.datahubterminalplugin.MavenTerminalExecutor.Target;
import io.intino.plugin.PluginLauncher.Notifier;
import io.intino.plugin.PluginLauncher.Phase;
import io.intino.plugin.PluginLauncher.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.ness.datahubterminalplugin.MavenTerminalExecutor.Target.Bpm;
import static io.intino.ness.datahubterminalplugin.MavenTerminalExecutor.Target.Events;
import static io.intino.plugin.PluginLauncher.Phase.*;

public class TerminalPublisher {
	private final File root;
	private final Terminal terminal;
	private final Configuration conf;
	private final Map<String, String> versions;
	private final SystemProperties systemProperties;
	private final String basePackage;
	private final Phase invokedPhase;
	private final PrintStream logger;
	private final Notifier notifier;

	public TerminalPublisher(File root, Terminal terminal, Configuration configuration, Map<String, String> versions, SystemProperties systemProperties, Phase invokedPhase, PrintStream logger, Notifier notifier) {
		this.root = root;
		this.terminal = terminal;
		this.conf = configuration;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		this.notifier = notifier;
	}

	public boolean publish() {
		try {
			if (!createSources()) return false;
			logger.println("Publishing " + terminal.name$() + "...");
			new MavenTerminalExecutor(root, basePackage, targets(), terminalNameArtifact(), versions, conf, systemProperties, logger).mvn(invokedPhase == INSTALL ? "install" : "deploy");
			logger.println("Terminal " + terminal.name$() + " published!");
			return true;
		} catch (Throwable e) {
			logger.println(e.getMessage() == null ? e.toString() : e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private ArrayList<Target> targets() {
		ArrayList<Target> targets = new ArrayList<>();
		targets.add(Events);
		if (terminal.bpm() != null) targets.add(Bpm);
		return targets;
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
		if (duplicatedEvents()) return false;
		new TerminalRenderer(terminal, srcDirectory, basePackage, conf, logger, notifier, conf.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(conf.artifact().name()).toString().toLowerCase()).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
		return true;
	}

	private boolean duplicatedEvents() {
		final Set<String> duplicatedPublish = terminal.publish() != null ? findDuplicates(terminal.publish().messageTanks().stream().map(Tank.Message::qn).collect(Collectors.toList())) : Collections.emptySet();
		final Set<String> duplicatedSubscribe = terminal.subscribe() != null ? findDuplicates(terminal.subscribe().messageTanks().stream().map(Tank.Message::qn).collect(Collectors.toList())) : Collections.emptySet();
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
		List<String> publish = terminal.publish() != null ? terminal.publish().messageTanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().messageTanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String terminalNameArtifact() {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().messageTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.message()).toLowerCase() + t.message().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().messageTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.message()).toLowerCase() + t.message().name$()));
		if (terminal.bpm() != null)
			tankClasses.put(terminal.bpm().processStatusClass().substring(terminal.bpm().processStatusClass().lastIndexOf(".") + 1), terminal.bpm().processStatusClass());
		return tankClasses;
	}

	private String eventQn(Tank.Message tank) {
		return namespace(tank.message()) + tank.message().name$();
	}

	private String namespace(Message message) {
		return message.core$().owner().is(Namespace.class) ? message.core$().ownerAs(Namespace.class).qn() + "." : "";
	}


}