package io.intino.ness.builder;

import io.intino.builder.*;
import io.intino.magritte.builder.MagrittecRunner;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static io.intino.builder.BuildConstants.*;
import static io.intino.builder.CompilerMessage.ERROR;


class NessCompilerRunner {
	private static final Logger LOG = Logger.getGlobal();

	private final boolean verbose;
	private final CompilerConfiguration config;
	private final Map<File, Boolean> sources;
	private PrintStream out = System.out;

	NessCompilerRunner(boolean verbose) {
		this.verbose = verbose;
		config = new CompilerConfiguration();
		sources = new LinkedHashMap<>();
	}

	void run(File argsFile) {
		CompilationInfoExtractor.getInfoFromArgsFile(argsFile, config, sources);
		config.setVerbose(verbose);
		config.out(System.out);
		this.out = config.out();
		if (config.mode().equals(Mode.Export)) compileExport();
		else MagrittecRunner.main(new String[]{argsFile.getAbsolutePath()});
		out.println();
		out.print(BUILD_END);
	}

	private void compileExport() {
		if (verbose) out.println(PRESENTABLE_MESSAGE + "nessc: loading sources...");
		final List<CompilerMessage> messages = new ArrayList<>();
		final List<PostCompileActionMessage> postCompileActionMessages = new ArrayList<>();
		new TerminalCompiler(config, messages, postCompileActionMessages).compile();
		out.println();
		processErrors(messages);
		if (messages.stream().noneMatch(m -> m.category().equalsIgnoreCase(ERROR)))
			processActions(postCompileActionMessages);
	}

	private void report(Map<File, Boolean> srcFiles, List<OutputItem> compiled) {
		if (compiled.isEmpty()) reportNotCompiledItems(srcFiles);
		else reportCompiledItems(compiled, srcFiles);
		out.println();
	}

	private void processErrors(List<CompilerMessage> compilerMessages) {
		int errorCount = 0;
		for (CompilerMessage message : compilerMessages) {
			if (message.category().equals(ERROR)) {
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
