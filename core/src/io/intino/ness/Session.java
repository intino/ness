package io.intino.ness;

import java.io.InputStream;

public interface Session {
	String SessionExtension = ".session";
	String EventSessionExtension = ".event.session";
	String name();

	Type type();

	InputStream inputStream();

	enum Type {
		event,
		set,
		setMetadata,
	}
}
