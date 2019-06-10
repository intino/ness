package io.intino.ness.triton.datalake.compiler;

import javax.tools.JavaFileManager.Location;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.tools.StandardLocation.SOURCE_PATH;

class JavaSourceFile extends SimpleJavaFileObject {
	private static final String JavaExtension = ".java";
	private final String source;

	JavaSourceFile(String source) {
		super(uriOf(SOURCE_PATH, SimpleParser.qualifiedClassNameIn(source)), Kind.SOURCE);
		this.source = source;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
		return source;
	}

	static URI uriOf(Location location, String packageName, String name) {
		return uriOf(location, packageName + "." + name);
	}

	static URI uriOf(Location location, String name) {
		try {
			return new URI(location + "/" + name.replaceAll("\\.", "/") + JavaExtension);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
