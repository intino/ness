package io.intino.ness.core;

import io.intino.ness.core.sessions.EventSession;
import io.intino.ness.core.sessions.SetSession;

import java.io.OutputStream;
import java.util.stream.Stream;


public interface Stage {

	OutputStream start(Blob.Type type);

	OutputStream start(String name, Blob.Type type);

	Stream<Blob> blobs();

	default SetSession createSetSession() {
		return new SetSession(this);
	}

	default SetSession createSetSession(int autoFlushSize) {
		return new SetSession(this, autoFlushSize);
	}

	default EventSession createEventSession() {
		return new EventSession(this);
	}

	void clear();

}
