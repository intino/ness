package io.intino.ness.terminal.builder;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Terminal;
import io.intino.ness.terminal.builder.codegeneration.Project;
import io.intino.ness.terminal.builder.codegeneration.ontology.OntologyBuilder;
import io.intino.ness.terminal.builder.codegeneration.terminal.TerminalBuilder;
import io.intino.ness.terminal.builder.util.ErrorUtils;
import io.intino.plugin.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.intino.ness.terminal.builder.VersionBounds.*;
import static io.intino.plugin.BuildConstants.PRESENTABLE_MESSAGE;

public class TerminalCompiler {
	private final CompilerConfiguration configuration;
	private final List<CompilerMessage> messages;
	private final List<PostCompileActionMessage> postCompileActionMessages;

	public TerminalCompiler(CompilerConfiguration configuration, List<CompilerMessage> messages, List<PostCompileActionMessage> postCompileActionMessages) {
		this.configuration = configuration;
		this.messages = messages;
		this.postCompileActionMessages = postCompileActionMessages;
	}

	public List<OutputItem> compile() {
		configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Building " + configuration.artifactId() + " terminals...");
		File tempDir = tempDirectory();
		try {
			run(tempDir);
		} catch (IOException | IntinoException e) {
			messages.add(new CompilerMessage(CompilerMessage.ERROR, ErrorUtils.getMessage(e)));
		}
		return List.of();
	}

	public void run(File tempDir) throws IOException, IntinoException {
		File outDirectory = configuration.outDirectory();
		if (!outDirectory.exists()) {
			messages.add(new CompilerMessage(CompilerMessage.ERROR, "Compiled model not found. Please compile module"));
			return;
		}
		NessGraph graph = loadGraph(outDirectory);
		if (hasErrors(graph)) return;
		Map<String, String> versions = versions();
		Project project = buildOntology(graph, versions, tempDir);
		postCompileActionMessages.add(actionMessage(project));
		List<Project> projects = buildTerminals(graph, versions, tempDir);
		projects.stream().map(this::actionMessage).forEach(postCompileActionMessages::add);
		configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Finished generation of terminals!");
	}

	private MavenBuildActionMessage actionMessage(Project project) {
		return new MavenBuildActionMessage(configuration.module(), project.pom(), project.coords(), configuration.phase().name());
	}

	private Map<String, String> versions() {
		return Map.of("terminal-jms", terminalJmsVersion(),
				"ingestion", ingestionVersion(),
				"bpm", bpmVersion(),
				"master", masterVersion(),
				"event", eventVersion(),
				"datalake", datalakeVersion(),
				"chronos", chronosVersion()
		);
	}

	private Project buildOntology(NessGraph graph, Map<String, String> versions, File tempDir) throws IntinoException {
		return new OntologyBuilder(new File(tempDir, "ontology"), graph, configuration, versions).build();
	}

	private List<Project> buildTerminals(NessGraph nessGraph, Map<String, String> versions, File tempDir) throws IntinoException {
		try {
			ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
			List<Future<Project>> futures = threadPool.invokeAll(nessGraph.terminalList().stream().map(terminal -> (Callable<Project>) () -> renderTerminal(versions, tempDir, terminal)).toList());
			threadPool.shutdownNow();
			threadPool.awaitTermination(10, TimeUnit.MINUTES);
			List<Project> projects = new ArrayList<>();
			for (Future<Project> future : futures) projects.add(future.get());
			return projects;
		} catch (Throwable e) {
			throw new IntinoException("Error building terminals: " + ErrorUtils.getMessage(e));
		}
	}

	private Project renderTerminal(Map<String, String> versions, File tempDir, Terminal terminal) throws IntinoException {
		return new TerminalBuilder(new File(tempDir, terminal.name$()), terminal, configuration, versions).build();
	}

	private NessGraph loadGraph(File outDirectory) {
		try (URLClassLoader urlClassLoader = new URLClassLoader(urlOf(outDirectory), this.getClass().getClassLoader())) {
			Class<?> aClass = urlClassLoader.loadClass(configuration.generationPackage() + "." + configuration.model().generationPackage() + ".GraphLoader");
			final NessGraph graph = (NessGraph) aClass.getMethod("load").invoke(null);
			if (graph.messageList(t -> t.name$().equals("Session")).findAny().isEmpty())
				graph.create("Session", "Session").message();
			return graph;
		} catch (ClassNotFoundException | NoSuchMethodException | IOException | IllegalAccessException |
				 InvocationTargetException e) {
			Logger.error(e);
			return null;
		}
	}

	private URL[] urlOf(File outDirectory) {
		try {
			return new URL[]{outDirectory.toPath().toUri().toURL()};
		} catch (MalformedURLException e) {
			Logger.error(e);
			return new URL[0];
		}
	}

	private boolean hasErrors(NessGraph graph) {
		if (graph == null) {
			messages.add(new CompilerMessage(CompilerMessage.ERROR, "Couldn't load graph. Please recompile module"));
			return true;
		}
		return false;
	}

	private File tempDirectory() {
		try {
			return Files.createTempDirectory("_temp").toFile();
		} catch (IOException e) {
			Logger.error(e);
			return new File("");
		}
	}
}