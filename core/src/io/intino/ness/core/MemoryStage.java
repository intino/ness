package io.intino.ness.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MemoryStage implements Stage {
	private List<MyOutputStream> streams = new ArrayList<>();

	@Override
	public OutputStream start(Blob.Type type) {
		return start(name(), type);
	}

	@Override
	public OutputStream start(String prefix, Blob.Type type) {
		streams.add(0, new MyOutputStream(name(prefix), type));
		return streams.get(0);
	}

	@Override
	public Stream<Blob> blobs() {
		return streams.stream()
				.map(s -> new Blob() {
					@Override
					public String name() {
						return s.name();
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

	private class MyOutputStream extends ByteArrayOutputStream {
		private final String name;
		private final Blob.Type type;

		public MyOutputStream(String name, Blob.Type type) {
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
