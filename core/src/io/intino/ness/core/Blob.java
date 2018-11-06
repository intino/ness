package io.intino.ness.core;

import java.io.InputStream;

public interface Blob {
	String name();
	Type type();
	InputStream inputStream();

	enum Type {
		event,
		set,
		setMetadata,
	}
}
