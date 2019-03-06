package io.intino.ness.core;

import java.io.IOException;

public interface EventWriter {
	void write(String str) throws IOException;

	void flush();

	void close();
}
