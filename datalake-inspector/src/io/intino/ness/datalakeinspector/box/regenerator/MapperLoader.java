package io.intino.ness.datalakeinspector.box.regenerator;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class MapperLoader {
	private final File home;

	public MapperLoader(File home) {
		this.home = new File(home, "mappers");
	}

	public Mapper compileAndLoad(String mapperCode) throws CompilationException, IOException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		mapperCode = mapperCode.trim();
		if (mapperCode.startsWith("package")) mapperCode = mapperCode.substring(mapperCode.indexOf("\n"));
		home.mkdirs();
		String className = nameOf(mapperCode);
		File file = javaFile(className);
		Files.writeString(file.toPath(), mapperCode);
		compile(file);
		return load(home, className);
	}

	public void delete(String mapperCode) {
		String name = nameOf(mapperCode);
		javaFile(name).delete();
		classFile(name).forEach(File::delete);
	}

	private void compile(File mapperJava) throws CompilationException, IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnits =
				fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(mapperJava));
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		JavaCompiler.CompilationTask task = compiler.getTask(
				null,
				fileManager,
				diagnostics,
				null,
				null,
				compilationUnits);
		task.call();
		fileManager.close();
		if (!diagnostics.getDiagnostics().isEmpty()) {
			throw new CompilationException(diagnostics.getDiagnostics().stream().map(d -> "Error on line " + d.getLineNumber() + ": " + d.getMessage(Locale.getDefault())).collect(Collectors.joining("\n")));
		}
	}

	private Mapper load(File home, String className) throws MalformedURLException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		URLClassLoader urlClassLoader = new URLClassLoader(
				new URL[]{home.toURI().toURL()},
				classLoader);
		Class javaDemoClass = urlClassLoader.loadClass(className);
		return (Mapper) javaDemoClass.getConstructors()[0].newInstance();
	}

	private String nameOf(String mapperCode) {
		String class_ = "class ";
		int index = mapperCode.indexOf(class_);
		return mapperCode.substring(index + class_.length(), mapperCode.indexOf(" ", index + class_.length()));
	}

	private File javaFile(String className) {
		return new File(home, className + ".java");
	}

	private List<File> classFile(String className) {
		List<File> files = new ArrayList<>();
		File e = new File(home, className + ".class");
		if (e.exists()) files.add(e);
		files.addAll(Arrays.stream(home.listFiles()).filter(f -> f.getName().startsWith(className + "$") && f.getName().endsWith(".class")).collect(Collectors.toList()));
		return files;
	}

	public static class CompilationException extends Exception {

		public CompilationException() {
		}

		public CompilationException(String message) {
			super(message);
		}

		public CompilationException(String message, Throwable cause) {
			super(message, cause);
		}

		public CompilationException(Throwable cause) {
			super(cause);
		}

		public CompilationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
}
