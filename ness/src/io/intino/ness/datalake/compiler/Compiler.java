package io.intino.ness.datalake.compiler;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class Compiler {

	private final List<JavaSourceFile> sources;
	private final DiagnosticCollector<JavaFileObject> diagnostics;
	private final JavaCompiler compiler;
	private final CompilerClassLoader classLoader;
	private final List<String> options;

	private Compiler(List<JavaSourceFile> sources) {
		this.sources = sources;
		this.compiler = checkTools(ToolProvider.getSystemJavaCompiler());
		this.classLoader = new CompilerClassLoader(this.getClass().getClassLoader());
		this.diagnostics = new DiagnosticCollector<>();
		this.options = new ArrayList<>();
	}

	public static Compiler compile(String... sources) {
		return compile(stream(sources).map(JavaSourceFile::new).collect(toList()));
	}

	@SuppressWarnings("unchecked")
	public static Compiler compile(List<JavaSourceFile> sources) {
		return new Compiler(sources);
	}

	public Compiler with(String... options) {
		this.options.addAll(asList(options));
		return this;
	}

	@SuppressWarnings("unchecked")
	public Result load(String className) {
		CompilerFileManager fileManager = new CompilerFileManager(standardFileManager(), classLoader);
		fileManager.putFilesForInput(sources);
		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sources);
		check(task.call());
		return new Result() {
			@Override
			public <T> Class<T> as(Class<T> type) throws ClassNotFoundException {
				return (Class<T>) classLoader.loadClass(className);
			}
		};
	}

	private void check(Boolean result) throws Exception {
		if (result != null && result) return;
		throw new Exception("Compilation failed", diagnostics);
	}

	private StandardJavaFileManager standardFileManager() {
		return compiler.getStandardFileManager(diagnostics, null, null);
	}

	private JavaCompiler checkTools(JavaCompiler compiler) {
		if (compiler != null) return compiler;
		throw new IllegalStateException("Cannot find the system Java compiler. " + "Check that your class path includes tools.jar");
	}

	public static class Exception extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final transient DiagnosticCollector<JavaFileObject> diagnostics;

		public Exception(String message, DiagnosticCollector<JavaFileObject> diagnostics) {
			super(message);
			this.diagnostics = diagnostics;
		}

		@Override
		public String toString() {
			String result = "";
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
				result += diagnostic.toString() + "\n";
			return result;
		}
	}

	public interface Result {
		<T> Class<T> as(Class<T> type) throws ClassNotFoundException;
	}
}

