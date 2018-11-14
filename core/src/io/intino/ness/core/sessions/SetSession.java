package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.triplestore.MemoryTripleStore;
import io.intino.ness.core.Blob;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Stage;
import io.intino.ness.core.Timetag;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class SetSession {
	private final SetSessionFileWriter writer;
	private final MemoryTripleStore.Builder tripleStore;
	private final int maxSize;
	private int count = 0;

	public SetSession(Stage stage) {
		this(stage, 1000000);
	}

	public SetSession(Stage stage, int autoFlushSize) {
		this.writer = new SetSessionFileWriter(zipStream(stage.start(Blob.Type.set)));
		this.tripleStore = new MemoryTripleStore.Builder(zipStream(stage.start(Blob.Type.setMetadata)));
		this.maxSize = autoFlushSize;
	}

	private OutputStream zipStream(OutputStream outputStream) {
		try {
			return new GZIPOutputStream(outputStream);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
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

	private void doFlush() {
		flush();
	}

	public void flush() {
		writer.flush();
		count = 0;
	}

	public void close() {
		writer.close();
		tripleStore.close();
	}

}
