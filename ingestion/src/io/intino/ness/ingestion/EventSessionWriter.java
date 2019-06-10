package io.intino.ness.ingestion;

public interface EventSessionWriter {

	void write(String text);

	void flush();

	void close();
}
