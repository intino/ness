package io.intino.ness.ingestion;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;

import java.io.*;
import java.util.*;

public class SetSessionFileReader {
	private final File file;
	private final Map<Fingerprint, List<Chunk>> chunks;

	public SetSessionFileReader(File file) throws IOException {
		this.file = file;
		this.chunks = chunksIn(this.file);
	}

	public Set<Fingerprint> fingerprints() {
		return chunks.keySet();
	}

	public List<ZetStream> streamsOf(Fingerprint fingerprint) {
		List<ZetStream> zetStreams = new ArrayList<>();
		for (Chunk chunk : chunks.getOrDefault(fingerprint, Collections.emptyList())) zetStreams.add(chunk.stream());
		return zetStreams;
	}

	private Map<Fingerprint, List<Chunk>> chunksIn(File file) throws IOException {
		Map<Fingerprint, List<Chunk>> chunks = new HashMap<>();
		fill(chunks, file);
		return chunks;
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private void fill(Map<Fingerprint, List<Chunk>> chunks, File file) throws IOException {
		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			long position = 0;
			while (true) {
				byte[] fingerprint = readData(stream);
				int size = skipData(stream);
				position += fingerprint.length + size + Integer.BYTES * 2;

				Chunk chunk = chunkOf(fingerprint, position - size, size);
				if (!chunks.containsKey(chunk.fingerprint)) chunks.put(chunk.fingerprint, new ArrayList<>());
				chunks.get(chunk.fingerprint).add(chunk);
			}
		} catch (EOFException ignored) {
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
			try {
				return new ZetReader(inputStream());
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

		private ByteArrayInputStream inputStream() throws IOException {
			return new ByteArrayInputStream(buffer());
		}

		private byte[] buffer() throws IOException {
			try (RandomAccessFile access = new RandomAccessFile(file, "r")) {
				byte[] buffer = new byte[size];
				access.seek(position);
				access.read(buffer);
				return buffer;
			}
		}
	}
}
