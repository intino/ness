package io.intino.ness.sealing;

import io.intino.ness.Session;

import java.util.stream.Stream;

public interface Stage {
	Stream<Session> sessions();

	void push(Stream<Session> sessions);

	void clear();
}