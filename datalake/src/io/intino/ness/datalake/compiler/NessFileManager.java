package io.intino.ness.datalake.compiler;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static io.intino.ness.datalake.compiler.JavaSourceFile.uriOf;

public final class NessFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final NessClassLoader classLoader;
    private final Map<URI, JavaFileObject> sources = new HashMap<>();

    public NessFileManager(JavaFileManager fileManager, NessClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String name) throws IOException {
        JavaFileObject result = sources.get(uriOf(location, packageName, name));
        return result != null ? result : super.getFileForInput(location, packageName, name);
    }

    public void putFilesForInput(List<JavaSourceFile> sources) {
        sources.forEach(this::putFileForInput);
    }

    public void putFileForInput(JavaSourceFile source) {
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
