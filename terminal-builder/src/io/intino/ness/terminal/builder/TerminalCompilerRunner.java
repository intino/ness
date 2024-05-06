package io.intino.ness.terminal.builder;

import io.intino.plugin.*;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static io.intino.plugin.BuildConstants.*;
import static io.intino.plugin.CompilerMessage.ERROR;


class TerminalCompilerRunner {
	private static final Logger LOG = Logger.getGlobal();

	private final boolean verbose;
	private PrintStream out = System.out;

	TerminalCompilerRunner(boolean verbose) {
		this.verbose = verbose;
	}

	void run(File argsFile) {
		final CompilerConfiguration config = new CompilerConfiguration();
		final Map<File, Boolean> sources = new LinkedHashMap<>();
		CompilationInfoExtractor.getInfoFromArgsFile(argsFile, config, sources);
		config.setVerbose(verbose);
		config.out(System.out);
		this.out = config.out();
		if (verbose) out.println(PRESENTABLE_MESSAGE + "Terminalc: loading sources...");
		final List<CompilerMessage> messages = new ArrayList<>();
		final List<PostCompileActionMessage> postCompileActionMessages = new ArrayList<>();
		List<OutputItem> compiled = compile(config, sources, messages, postCompileActionMessages);
		if (verbose) report(sources, compiled);
		processErrors(messages);
		if (messages.stream().noneMatch(m -> m.category().equalsIgnoreCase(ERROR)))
			processActions(postCompileActionMessages);
		out.println();
		out.print(BUILD_END);
	}

	private List<OutputItem> compile(CompilerConfiguration config, Map<File, Boolean> sources, List<CompilerMessage> messages, List<PostCompileActionMessage> postCompileActionMessages) {
		return new ArrayList<>(compileSources(config, messages, postCompileActionMessages));
	}

	private List<OutputItem> compileSources(CompilerConfiguration config, List<CompilerMessage> messages, List<PostCompileActionMessage> postCompileActionMessages) {
		List<OutputItem> outputItems = new TerminalCompiler(config, messages, postCompileActionMessages).compile();
		out.println();
		return outputItems;
	}

	private void report(Map<File, Boolean> srcFiles, List<OutputItem> compiled) {
		if (compiled.isEmpty()) reportNotCompiledItems(srcFiles);
		else reportCompiledItems(compiled, srcFiles);
		out.println();
	}

	private void processErrors(List<CompilerMessage> compilerMessages) {
		int errorCount = 0;
		for (CompilerMessage message : compilerMessages) {
			if (message.category().equals(CompilerMessage.ERROR)) {
				if (errorCount > 100) continue;
				errorCount++;
			}
			printMessage(message);
		}
	}

	private void processActions(List<PostCompileActionMessage> postCompileActionMessages) {
		if (!postCompileActionMessages.isEmpty()) {
			out.print(START_ACTIONS_MESSAGE);
			postCompileActionMessages.forEach(this::printMessage);
			out.print(END_ACTIONS_MESSAGE);
		}
	}

	private void printMessage(CompilerMessage message) {
		out.print(MESSAGES_START);
		out.print(message.getCategoryLabel());
		out.print(SEPARATOR);
		out.print(message.message());
		out.print(SEPARATOR);
		out.print(message.url());
		out.print(SEPARATOR);
		out.print(message.lineNum());
		out.print(SEPARATOR);
		out.print(message.columnNum());
		out.print(SEPARATOR);
		out.print(MESSAGES_END);
		out.println();
	}

	private void printMessage(PostCompileActionMessage message) {
		out.print(MESSAGE_ACTION_START);
		out.print(message.toString());
		out.print(MESSAGE_ACTION_END);
		out.println();
	}

	private void reportCompiledItems(List<OutputItem> compiledFiles, Map<File, Boolean> srcFiles) {
		for (OutputItem compiledFile : compiledFiles) {
			out.print(COMPILED_START);
			out.print(compiledFile.getOutputPath());
			out.print(SEPARATOR);
			out.print(new File(compiledFile.getSourcePath()).isFile() ? compiledFile.getSourcePath() : srcFiles.keySet().iterator().next().getAbsolutePath());
			out.print(COMPILED_END);
			out.println();
		}
	}

	private void reportNotCompiledItems(Map<File, Boolean> toRecompile) {
		for (File file : toRecompile.keySet()) {
			out.print(TO_RECOMPILE_START);
			out.print(file.getAbsolutePath());
			out.print(TO_RECOMPILE_END);
			out.println();
		}
	}
}
