package io.intino.ness.ingestion;

import java.util.stream.Stream;

public interface Stage {
	Stream<Session> sessions();

	void push(Stream<Session> sessions);

	void clear();
}