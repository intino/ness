package io.intino.ness.core.sessions;

public interface EventSessionWriter {

	void write(String text);

	void flush();

	void close();
}
