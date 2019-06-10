package io.intino.ness.datalake.hadoop.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.ingestion.Fingerprint;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class SetSessionReader {
	private final Map<Fingerprint, List<Chunk>> chunks;
	private final byte[] session;

	public SetSessionReader(byte[] session) throws IOException {
		this.session = session;
		this.chunks = chunks();
	}

	public Set<Fingerprint> fingerprints() {
		return chunks.keySet();
	}

	public List<ZetStream> streamsOf(Fingerprint fingerprint) {
		List<ZetStream> zetStreams = new ArrayList<>();
		for (Chunk chunk : chunks.getOrDefault(fingerprint, Collections.emptyList())) zetStreams.add(chunk.stream());
		return zetStreams;
	}

	private Map<Fingerprint, List<Chunk>> chunks() throws IOException {
		Map<Fingerprint, List<Chunk>> chunks = new HashMap<>();
		fill(chunks);
		return chunks;
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private void fill(Map<Fingerprint, List<Chunk>> chunks) throws IOException {
		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(session)))) {
			long position = 0;
			while (true) {
				byte[] fingerprint = readData(stream);
				int size = skipData(stream);
				position += fingerprint.length + size + Integer.BYTES * 2;

				Chunk chunk = chunkOf(fingerprint, position - size, size);
				if (!chunks.containsKey(chunk.fingerprint)) chunks.put(chunk.fingerprint, new ArrayList<>());
				chunks.get(chunk.fingerprint).add(chunk);
			}
		}
	}

	private Chunk chunkOf(byte[] fingerprint, long position, int size) {
		return chunkOf(new Fingerprint(new String(fingerprint)), position, size);
	}

	private Chunk chunkOf(Fingerprint fingerprint, long position, int size) {
		return new Chunk(fingerprint, position, size);
	}

	private byte[] readData(DataInputStream stream) throws IOException {
		int size = stream.readInt();
		byte[] data = new byte[size];
		stream.read(data);
		return data;
	}

	private int skipData(DataInputStream stream) throws IOException {
		int size = stream.readInt();
		stream.skipBytes(size);
		return size;
	}

	public class Chunk {
		private final Fingerprint fingerprint;
		private final long position;
		private final int size;

		Chunk(Fingerprint fingerprint, long position, int size) {
			this.fingerprint = fingerprint;
			this.position = position;
			this.size = size;
		}

		public String tank() {
			return fingerprint.tank();
		}

		public Timetag timetag() {
			return fingerprint.timetag();
		}

		public String set() {
			return fingerprint.set();
		}

		public ZetStream stream() {
			return new ZetReader(inputStream());
		}

		private ByteArrayInputStream inputStream() {
			return new ByteArrayInputStream(buffer());
		}

		private byte[] buffer() {
			return Arrays.copyOfRange(session, (int) position, (int) position + size);
		}
	}
}
