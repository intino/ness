package io.intino.ness.master.messages.handlers;

import io.intino.ness.master.messages.MasterMessage;
import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.MasterMessageSerializer;

public interface MasterMessageHandler<T extends MasterMessage> {

	default void handle(String serializedMessage) throws MasterMessageException {
		handle(MasterMessageSerializer.deserialize(serializedMessage, messageClass()));
	}

	void handle(T message) throws MasterMessageException;

	Class<T> messageClass();
}
