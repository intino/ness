package io.intino.ness.datalake.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.intino.ness.datalake.compiler.JavaSourceFile.uriOf;

final class CompilerFileManager extends ForwardingJavaFileManager<JavaFileManager> {
	private final CompilerClassLoader classLoader;
	private final Map<URI, JavaFileObject> sources = new HashMap<>();

	CompilerFileManager(JavaFileManager fileManager, CompilerClassLoader classLoader) {
		super(fileManager);
		this.classLoader = classLoader;
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName, String name) throws IOException {
		JavaFileObject result = sources.get(uriOf(location, packageName, name));
		return result != null ? result : super.getFileForInput(location, packageName, name);
	}

	void putFilesForInput(List<JavaSourceFile> sources) {
		sources.forEach(this::putFileForInput);
	}

	private void putFileForInput(JavaSourceFile source) {
		sources.put(source.toUri(), source);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject outputFile) throws IOException {
		JavaFileObject file = new JavaClassFile(className, kind);
		classLoader.add(className, file);
		return file;
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return super.getClassLoader(location);
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		return super.inferBinaryName(location, file);
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
		return super.list(location, packageName, kinds, recurse);
	}

}
