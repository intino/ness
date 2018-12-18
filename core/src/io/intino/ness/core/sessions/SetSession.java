package io.intino.ness.core.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.triplestore.MemoryTripleStore;
import io.intino.ness.core.Blob;
import io.intino.ness.core.BlobHandler;
import io.intino.ness.core.Datalake;

import java.util.Arrays;
import java.util.stream.Stream;

public class SetSession {
	private final SetSessionFileWriter writer;
	private final MemoryTripleStore.Builder tripleStore;
	private final int maxSize;
	private int count = 0;

	public SetSession(BlobHandler blobHandler) {
		this(blobHandler, 1000000);
	}

	public SetSession(BlobHandler blobHandler, int autoFlushSize) {
		this.writer = new SetSessionFileWriter(blobHandler.start(Blob.Type.set));
		this.tripleStore = new MemoryTripleStore.Builder(blobHandler.start(Blob.Type.setMetadata));
		this.maxSize = autoFlushSize;
	}

	public void put(String tank, Timetag timetag, String set, Stream<Long> ids) {
		ids.forEach(i -> writer.add(tank, timetag, set, i));
		if (count++ >= maxSize) doFlush();
	}

	public void put(String tank, Timetag timetag, String set, long... ids) {
		put(tank, timetag, set, Arrays.stream(ids).boxed());
	}

	public void define(String tank, Timetag timetag, String set, Datalake.SetStore.Variable variable) {
		tripleStore.put(Fingerprint.of(tank, timetag, set).toString(), variable.name, variable.value);
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
