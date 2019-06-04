package io.intino.ness.datalake.file.eventsourcing;

import io.intino.alexandria.inl.Message;

public interface EventHandler {
	void handle(Message message);
}
