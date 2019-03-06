package io.intino.ness.core;

import java.io.InputStream;

public interface Session {
	String name();

	Type type();

	InputStream inputStream();

	enum Type {
		event,
		set,
		setMetadata,
	}
}
