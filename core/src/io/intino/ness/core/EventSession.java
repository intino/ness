package io.intino.ness.core;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;

public interface EventSession {
	void put(String tank, Timetag timetag, Message... messages);

	void flush();

	void close();
}
