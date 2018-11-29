package io.intino.ness.core;

import io.intino.ness.core.sessions.EventSession;
import io.intino.ness.core.sessions.SetSession;

import java.util.stream.Stream;


public interface Stage {

	Stream<Blob> blobs();

	SetSession createSetSession();

	SetSession createSetSession(int autoFlushSize);

	EventSession createEventSession();

	void discard();

}
