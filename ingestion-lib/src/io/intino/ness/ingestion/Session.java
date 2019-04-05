package io.intino.ness.ingestion;

import java.io.InputStream;

public interface Session {
	String SessionExtension = ".blob";
	String EventSessionExtension = ".event.blob";
	String name();

	Type type();

	InputStream inputStream();

	enum Type {
		event,
		set,
		setMetadata,
	}
}
