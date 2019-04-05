package io.intino.ness.ingestion;

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
