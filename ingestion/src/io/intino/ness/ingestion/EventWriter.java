package io.intino.ness.ingestion;

import java.io.IOException;

public interface EventWriter {
	void write(String str) throws IOException;

	void flush();

	void close();
}
