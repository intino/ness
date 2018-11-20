package io.intino.ness.core.memory;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Blob;
import io.intino.ness.core.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MemoryStage implements Stage {
	private List<MemoryOutputStream> streams = new ArrayList<>();

	@Override
	public OutputStream start(Blob.Type type) {
		return start("", type);
	}

	@Override
	public OutputStream start(String prefix, Blob.Type type) {
		try {
			streams.add(0, new MemoryOutputStream(name(prefix), type));
			return streams.get(0);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
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
						return new ByteArrayInputStream(s.toByteArray());
					}
				});
	}

	@Override
	public void clear() {
		streams.clear();
	}

	private class MemoryOutputStream extends ByteArrayOutputStream {
		private final String name;
		private final Blob.Type type;

		public MemoryOutputStream(String name, Blob.Type type) throws IOException {
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
