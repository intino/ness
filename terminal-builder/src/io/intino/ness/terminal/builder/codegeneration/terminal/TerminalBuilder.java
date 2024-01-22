package io.intino.ness.terminal.builder.codegeneration.terminal;

import com.google.gson.Gson;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Terminal;
import io.intino.magritte.framework.Layer;
import io.intino.ness.terminal.builder.IntinoException;
import io.intino.ness.terminal.builder.codegeneration.Formatters;
import io.intino.ness.terminal.builder.codegeneration.PomRenderer;
import io.intino.ness.terminal.builder.codegeneration.Project;
import io.intino.ness.terminal.builder.util.ErrorUtils;
import io.intino.plugin.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.BuildConstants.PRESENTABLE_MESSAGE;

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
		checkDuplicatedEvents();
		new TerminalRenderer(terminal, srcDirectory, basePackage, configuration).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
	}

	private void checkDuplicatedEvents() throws IntinoException {
		// If an error raises from here, terminal in .tara probably mixes tanks from various types (messages, measurements or resources)
		Set<String> duplicatedPublish = terminal.publish() != null
				? findDuplicates(terminal.publish().messageTanks().stream().map(Tank.Message::qn).toList())
				: Collections.emptySet();

		Set<String> duplicatedSubscribe = terminal.subscribe() != null
				? findDuplicates(terminal.subscribe().messageTanks().stream().map(Tank.Message::qn).toList())
				: Collections.emptySet();

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
		List<String> publish = terminal.publish() != null ? terminal.publish().messageTanks().stream().map(this::eventQn).collect(Collectors.toList()) : new ArrayList<>();
		if (terminal.publish() != null) {
			publish.addAll(terminal.publish().measurementTanks().stream().map(this::eventQn).toList());
			publish.addAll(terminal.publish().resourceTanks().stream().map(this::eventQn).toList());
		}
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().messageTanks().stream().map(this::eventQn).collect(Collectors.toList()) : new ArrayList<>();
		if (terminal.subscribe() != null) {
			subscribe.addAll(terminal.subscribe().measurementTanks().stream().map(this::eventQn).toList());
			subscribe.addAll(terminal.subscribe().resourceTanks().stream().map(this::eventQn).toList());
		}
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), terminal.datamarts() != null && terminal.datamarts().autoLoad());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}


	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null) {
			terminal.publish().messageTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".messages." + eventQn(t)));
			terminal.publish().measurementTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".measurements." + eventQn(t)));
			terminal.publish().resourceTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".resources." + eventQn(t)));
		}
		if (terminal.subscribe() != null) {
			terminal.subscribe().messageTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".messages." + eventQn(t)));
			terminal.subscribe().measurementTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".measurements." + eventQn(t)));
			terminal.subscribe().resourceTanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".resources." + eventQn(t)));
		}
		if (terminal.bpm() != null)
			tankClasses.put(terminal.bpm().processStatusClass().substring(terminal.bpm().processStatusClass().lastIndexOf(".") + 1), terminal.bpm().processStatusClass());
		return tankClasses;
	}

	private String eventQn(Tank.Message tank) {
		return namespace(tank.message()) + tank.message().name$();
	}

	private String eventQn(Tank.Resource tank) {
		return namespace(tank.resourceEvent()) + tank.resourceEvent().name$();
	}

	private String eventQn(Tank.Measurement tank) {
		return namespace(tank.sensor()) + tank.sensor().name$();
	}

	private String namespace(Layer event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn() + "." : "";
	}
}