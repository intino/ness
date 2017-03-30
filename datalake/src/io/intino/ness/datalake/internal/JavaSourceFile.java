package io.intino.ness.datalake.internal;

import javax.tools.JavaFileManager.Location;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.net.URISyntaxException;

import static io.intino.ness.datalake.internal.SimpleParser.qualifiedClassNameIn;
import static javax.tools.StandardLocation.SOURCE_PATH;

public class JavaSourceFile extends SimpleJavaFileObject {
    static final String JavaExtension = ".java";
    private final String source;

    public JavaSourceFile(String source) {
        super(uriOf(SOURCE_PATH, qualifiedClassNameIn(source)), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
        return source;
    }

    public static URI uriOf(Location location, String packageName, String name) {
        return uriOf(location, packageName + "." + name);
    }

    public static URI uriOf(Location location, String name) {
        try {
            return new URI(location + "/" + name.replaceAll("\\.","/") + JavaExtension);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
