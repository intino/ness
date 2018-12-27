package io.intino.ness.core.memory;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Blob;
import io.intino.ness.core.BlobHandler;
import io.intino.ness.core.Stage;
import io.intino.ness.core.sessions.EventSession;
import io.intino.ness.core.sessions.SetSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static java.util.UUID.randomUUID;

public class MemoryStage implements Stage, BlobHandler {
	private List<MemoryOutputStream> streams = new ArrayList<>();

	@Override
	public OutputStream start(Blob.Type type) {
		return start("", type);
	}

	@Override
	public OutputStream start(String name, Blob.Type type) {
		streams.add(0, new MemoryOutputStream(name + suffix(), type));
		return streams.get(0);
	}

	private String suffix() {
		return "#" + randomUUID().toString();
	}

	@Override
	public SetSession createSetSession() {
		return new SetSession(this);
	}

	@Override
	public SetSession createSetSession(int autoFlushSize) {
		return new SetSession(this, autoFlushSize);
	}

	@Override
	public EventSession createEventSession() {
		return new EventSession(this);
	}

	@Override
	public void clear() {
		streams.clear();
	}

	@Override
	public Stream<Blob> blobs() {
		return streams.stream()
				.map(s -> new Blob() {
					@Override
					public String name() {
						return s.name() + "." + s.type();
					}

					@Override
					public Type type() {
						return s.type();
					}

					@Override
					public InputStream inputStream() {
						try {
							return new GZIPInputStream(new ByteArrayInputStream(s.toByteArray()));
						} catch (IOException e) {
							Logger.error(e);
							return new ByteArrayInputStream(new byte[]{});
						}
					}
				});
	}

	private class MemoryOutputStream extends ByteArrayOutputStream {
		private final String name;
		private final Blob.Type type;

		public MemoryOutputStream(String name, Blob.Type type) {
			this.name = name;
			this.type = type;
		}

		public String name() {
			return name;
		}

		public Blob.Type type() {
			return type;
		}

	}
}
