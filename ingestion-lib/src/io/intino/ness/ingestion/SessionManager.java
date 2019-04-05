package io.intino.ness.ingestion;

import java.util.stream.Stream;

public interface SessionManager {

	void push(Stream<Session> sessions);

	void seal();
}
