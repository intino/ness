package io.intino.ness.core;

import java.util.stream.Stream;

public interface Stage {
	Stream<Session> sessions();

	void push(Stream<Session> sessions);

	void clear();
}