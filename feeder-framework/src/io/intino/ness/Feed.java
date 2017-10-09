package io.intino.ness;

import io.intino.ness.inl.Message;

public interface Feed {
	void send(Message message);

	default void flush() {
	}
}
