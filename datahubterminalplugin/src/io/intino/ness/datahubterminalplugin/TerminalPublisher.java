package io.intino.ness.datahubterminalplugin;

import com.google.gson.Gson;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Message;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.legio.graph.LegioGraph;
import io.intino.legio.graph.Repository;
import io.intino.ness.datahubterminalplugin.schema.MessageRenderer;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

class TerminalPublisher {
	private final File root;
	private final Terminal terminal;
	private final LegioGraph conf;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private List<Tank.Event> tanks;

	TerminalPublisher(File root, Terminal terminal, List<Tank.Event> tanks, LegioGraph configuration, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger) {
		this.root = root;
		this.terminal = terminal;
		this.tanks = tanks;
		this.conf = configuration;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name$()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
	}

	boolean publish() {
		if (!createSources()) return false;
		try {
			mvn(invokedPhase == PluginLauncher.Phase.INSTALL ? "install" : "deploy");
		} catch (IOException | MavenInvocationException e) {
			logger.println(e.getMessage());
			return false;
		}
		return true;
	}

	private boolean createSources() {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		Map<Message, Context> messageContextMap = collectMessages(tanks);
		new TerminalRenderer(terminal, messageContextMap, srcDirectory, basePackage).render();
		messageContextMap.forEach((k, v) -> new MessageRenderer(k, v, srcDirectory, basePackage).render());
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
		return true;
	}

	private void writeManifest(File srcDirectory) {
		List<String> publish = terminal.publish() != null ? terminal.publish().tanks().stream().map(Tank.Event::qn).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().tanks().stream().map(Tank.Event::qn).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), messageContexts());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Map<String, Set<String>> messageContexts() {
		Map<String, Set<String>> messageContexts = terminal.publish() == null ? new HashMap<>() : messageContextOf(terminal.publish().tanks());
		if (terminal.subscribe() == null) return messageContexts;
		Map<String, Set<String>> subscribeMessageContexts = messageContextOf(terminal.subscribe().tanks());
		for (String messageType : subscribeMessageContexts.keySet()) {
			if (!messageContexts.containsKey(messageType)) messageContexts.put(messageType, new HashSet<>());
			messageContexts.get(messageType).addAll(subscribeMessageContexts.get(messageType));
		}
		return messageContexts;
	}

	private Map<String, Set<String>> messageContextOf(List<Tank.Event> tanks) {
		return tanks.stream().
				collect(Collectors.toMap(t -> t.asTank().message().name$(),
						tank -> tank.asTank().isContextual() ? tank.asTank().asContextual().context().leafs().stream().map(Context::qn).collect(Collectors.toSet()) : Collections.emptySet(),
						(a, b) -> b));
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(t -> tankClasses.putIfAbsent(t.qn(), basePackage + ".schemas." + t.asTank().message().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(t -> tankClasses.putIfAbsent(t.qn(), basePackage + ".schemas." + t.asTank().message().name$()));
		if (terminal.allowsBpmIn() != null) {
			Context context = terminal.allowsBpmIn().context();
			String statusQn = terminal.allowsBpmIn().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			tankClasses.put((context != null ? context.qn() + "." : "") + statusClassName, statusQn);
		}
		return tankClasses;
	}

	private Map<Message, Context> collectMessages(List<Tank.Event> tanks) {
		Map<Message, Context> messages = new HashMap<>();
		for (Tank.Event tank : tanks) {
			List<Message> hierarchy = hierarchy(tank.message());
			Context context = tank.asTank().isContextual() ? tank.asTank().asContextual().context() : null;
			messages.put(hierarchy.get(0), context);
			hierarchy.remove(0);
			hierarchy.forEach(message -> messages.put(message, null));
		}
		return messages;
	}

	private List<Message> hierarchy(Message message) {
		Set<Message> messages = new LinkedHashSet<>();
		messages.add(message);
		if (message.isExtensionOf()) messages.addAll(hierarchy(message.asExtensionOf().parent()));
		return new ArrayList<>(messages);
	}

	private void mvn(String goal) throws IOException, MavenInvocationException {
		final File pom = createPom(root, basePackage, terminal.name$(), conf.artifact().version());
		final InvocationResult result = invoke(pom, goal);
		if (result != null && result.getExitCode() != 0) {
			if (result.getExecutionException() != null)
				throw new IOException("Failed to publish accessor.", result.getExecutionException());
			else throw new IOException("Failed to publish accessor. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed to publish accessor. Maven HOME not found");
	}

	private InvocationResult invoke(File pom, String goal) throws MavenInvocationException {
		List<String> goals = new ArrayList<>();
		goals.add("clean");
		goals.add("install");
		if (!goal.isEmpty()) goals.add(goal);
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(goals);
		logger.println("Maven HOME: " + systemProperties.mavenHome.getAbsolutePath());
		Invoker invoker = new DefaultInvoker().setMavenHome(systemProperties.mavenHome);
		log(invoker);
		config(request, systemProperties.mavenHome);
		return invoker.execute(request);
	}

	private void log(Invoker invoker) {
		invoker.setErrorHandler(logger::println);
		invoker.setOutputHandler(logger::println);
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		request.setJavaHome(systemProperties.javaHome);
	}


	private File createPom(File root, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", artifact).add("version", version);
		conf.repositoryList().forEach(r -> buildRepoFrame(builder, r));
		if (terminal.allowsBpmIn() != null) {
			builder.add("hasBpm", ";");
		}
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private void buildRepoFrame(FrameBuilder builder, Repository r) {
		builder.add("repository", r.releaseList().stream().map(release -> createRepositoryFrame(r, release)).toArray(Frame[]::new));
	}

	private Frame createRepositoryFrame(Repository repository, Repository.Release release) {
		return new FrameBuilder("repository", isDistribution(release) ? "distribution" : "release").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", release.url()).toFrame();
	}

	private boolean isDistribution(Repository.Release release) {
		return conf.artifact().distribution() != null && release.equals(conf.artifact().distribution().release());
	}
}
