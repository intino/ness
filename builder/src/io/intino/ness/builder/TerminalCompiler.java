package io.intino.ness.builder;

import io.intino.alexandria.logger.Logger;
import io.intino.builder.*;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Terminal;
import io.intino.ness.builder.codegeneration.Project;
import io.intino.ness.builder.codegeneration.ontology.OntologyBuilder;
import io.intino.ness.builder.codegeneration.terminal.TerminalBuilder;
import io.intino.ness.builder.util.ErrorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;


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
		configuration.out().println(PRESENTABLE_MESSAGE + "nessc: Building " + configuration.artifactId() + " terminals...");
		try {
			File tempDir = configuration.getTempDirectory();
			tempDir.mkdirs();
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
		configuration.out().println(PRESENTABLE_MESSAGE + "nessc: Finished generation of terminals!");
	}

	private ArtifactBuildActionMessage actionMessage(Project project) {
		return new ArtifactBuildActionMessage(configuration.module(), project.pom(), project.coords(), configuration.invokedPhase().name());
	}

	private Map<String, String> versions() {
		return Map.of("terminal-jms", VersionBounds.terminalJmsVersion(),
				"ingestion", VersionBounds.ingestionVersion(),
				"bpm", VersionBounds.bpmVersion(),
				"master", VersionBounds.masterVersion(),
				"event", VersionBounds.eventVersion(),
				"datalake", VersionBounds.datalakeVersion(),
				"chronos", VersionBounds.chronosVersion()
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
			Class<?> aClass = urlClassLoader.loadClass(findClass(outDirectory));
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

	private String findClass(File outDirectory) {
		File file = new File(outDirectory, configuration.generationPackage().replace(".", File.separator));
		Collection<File> files = FileUtils.listFiles(file, new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().equals("GraphLoader.class");
			}

			@Override
			public boolean accept(File file, String s) {
				return false;
			}
		}, new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return true;
			}

			@Override
			public boolean accept(File file, String s) {
				return true;
			}
		});
		return files.isEmpty() ? null : files.iterator().next().getAbsolutePath()
				.replace(outDirectory.getAbsolutePath() + File.separator, "")
				.replace(File.separator, ".")
				.replace(".class", "");
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
}