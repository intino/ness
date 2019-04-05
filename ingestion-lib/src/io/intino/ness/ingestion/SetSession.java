package io.intino.ness.ingestion;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.triplestore.MemoryTripleStore;

import java.util.Arrays;
import java.util.stream.Stream;

public class SetSession {
	private final SetSessionWriter writer;
	private final MemoryTripleStore.Builder tripleStore;
	private final int maxSize;
	private int count = 0;

	public SetSession(SessionHandler.Provider provider, SetSessionWriter writer) {
		this(provider, writer, 1000000);
	}

	public SetSession(SessionHandler.Provider provider, SetSessionWriter writer, int autoFlushSize) {
		this.writer = writer;
		this.tripleStore = new MemoryTripleStore.Builder(provider.outputStream(Session.Type.setMetadata));
		this.maxSize = autoFlushSize;
	}

	public void put(String tank, Timetag timetag, String set, Stream<Long> ids) {
		ids.forEach(i -> writer.add(tank, timetag, set, i));
		if (count++ >= maxSize) doFlush();
	}

	public void put(String tank, Timetag timetag, String set, long... ids) {
		put(tank, timetag, set, Arrays.stream(ids).boxed());
	}

	public void define(String tank, Timetag timetag, String set, String variable, String value) {
		tripleStore.put(Fingerprint.of(tank, timetag, set).toString(), variable, value);
	}

	public void flush() {
		writer.flush();
		count = 0;
	}

	public void close() {
		writer.close();
		tripleStore.close();
	}

	private void doFlush() {
		flush();
	}

}
