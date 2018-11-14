package io.intino.ness.datalake.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

class JavaClassFile extends SimpleJavaFileObject {
	private ByteArrayOutputStream byteCode;

	JavaClassFile(String className, Kind kind) {
		super(uriOf(className), kind);
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("getCharContent()");
	}

	@Override
	public InputStream openInputStream() {
		return new ByteArrayInputStream(byteCode.toByteArray());
	}

	@Override
	public OutputStream openOutputStream() {
		return byteCode = new ByteArrayOutputStream();
	}

	byte[] getByteCode() {
		return byteCode.toByteArray();
	}

	private static URI uriOf(String name) {
		try {
			return new URI(name);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}


}
