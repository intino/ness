package io.intino.ness.datalake.compiler;

import javax.tools.JavaFileObject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class NessClassLoader extends ClassLoader {
    private final Map<String, JavaFileObject> classes = new HashMap<>();

    public NessClassLoader(ClassLoader parent) {
        super(parent);
    }

    void add(String className, JavaFileObject javaFile) {
        classes.put(className, javaFile);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (!exists(className)) return super.loadClass(className);
        return createClass(className);

    }

    private boolean exists(String className) {
        return classes.containsKey(className);
    }

    private Class<?> createClass(String className) {
        return createClass(className, bytesOf(className));
    }

    private Class<?> createClass(String className, byte[] bytes) {
        return bytes != null ? defineClass(className, bytes, 0, bytes.length) : null;
    }

    private byte[] bytesOf(String className) {
        return bytesOf(classes.get(className));
    }

    private byte[] bytesOf(JavaFileObject file) {
        return file != null ? ((JavaClassFile) file).getByteCode() : null;
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        return super.getResourceAsStream(name);
    }
}
