package io.intino.ness.datalake;

import io.intino.ness.datalake.internal.NessClassLoader;
import io.intino.ness.datalake.internal.NessFileManager;
import io.intino.ness.datalake.internal.JavaSourceFile;

import java.util.*;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class NessCompiler {

    private final List<JavaSourceFile> sources;
    private final JavaCompiler compiler;
    private final NessClassLoader classLoader;
    private final DiagnosticCollector<JavaFileObject> diagnostics;
    private final List<String> options;

    private NessCompiler(List<JavaSourceFile> sources) {
        this.sources = sources;
        this.compiler = checkTools(ToolProvider.getSystemJavaCompiler());
        this.classLoader = new NessClassLoader(this.getClass().getClassLoader());
        this.diagnostics = new DiagnosticCollector<>();
        this.options = new ArrayList<>();
    }

    public static NessCompiler compile(String... sources)  {
        return compile(stream(sources).map(JavaSourceFile::new).collect(toList()));
    }

    @SuppressWarnings("unchecked")
    public static NessCompiler compile(List<JavaSourceFile> sources) {
        return new NessCompiler(sources);
    }

    public NessCompiler with(String... options) {
        this.options.addAll(asList(options));
        return this;
    }

    @SuppressWarnings("unchecked")
    public Result load(String className) {
        NessFileManager fileManager = new NessFileManager(standardFileManager(), classLoader);
        fileManager.putFilesForInput(sources);
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sources);
        check(task.call());
        return new Result() {
            @Override
            public <T> Class<T> as(Class<T> type) {
                try {
                    return (Class<T>) classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    return null;
                }
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
        throw new IllegalStateException("Cannot find the system Java internal. " + "Check that your class path includes tools.jar");
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
        <T> Class<T> as(Class<T> type);
    }
}

