package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.logger.Logger;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Locale;

public class MapperLoader {
	private final File home;

	public MapperLoader(File home) {
		this.home = home;
	}

	public Mapper compileAndLoad(String mapperCode) {
		mapperCode = mapperCode.trim();
		if (mapperCode.startsWith("package")) mapperCode = mapperCode.substring(mapperCode.indexOf("\n"));
		File home = new File(this.home, "mappers");
		home.mkdirs();
		String className = nameOf(mapperCode);
		File file = new File(home, className + ".java");
		try {
			Files.writeString(file.toPath(), mapperCode);
			compile(file);
			return load(home, className);
		} catch (IOException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | InstantiationException e) {
			Logger.error(e);
		}
		return null;
	}

	private void compile(File mapperJava) throws IOException {
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
		for (Diagnostic diagnostic : diagnostics.getDiagnostics())
			System.out.format("Error on line %d: %s",
					diagnostic.getLineNumber(),
					diagnostic.getMessage(Locale.getDefault()));
		fileManager.close();
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
}
