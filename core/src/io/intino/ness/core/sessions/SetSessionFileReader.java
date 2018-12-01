package io.intino.ness.core.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SetSessionFileReader {
	private final File file;
	private final File tempFolder;
	private final Map<Fingerprint, List<Chunk>> chunks;

	public SetSessionFileReader(File file, File tempFolder) throws IOException {
		this.tempFolder = tempFolder;
		this.file = unzip(file);
		this.chunks = chunksIn(this.file);
	}

	public Set<Fingerprint> fingerprints() {
		return chunks.keySet();
	}

	public List<ZetStream> streamsOf(Fingerprint fingerprint) {
		List<ZetStream> zetStreams = new ArrayList<>();
		for (Chunk chunk : chunks.get(fingerprint)) zetStreams.add(chunk.stream());
		return zetStreams;
	}

	private File unzip(File file) throws IOException {
		File tempFile = tempFile(file.getName());
		try (InputStream inputStream = gzipInputStreamOf(file)) {
			Files.copy(inputStream, tempFile.toPath(), REPLACE_EXISTING);
		}
		return tempFile;
	}

	private File tempFile(String name) throws IOException {
		return File.createTempFile(name.substring(0, name.indexOf(".")), ".chunks", tempFolder);
	}

	private GZIPInputStream gzipInputStreamOf(File file) throws IOException {
		return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
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
				Fingerprint fingerprint = new Fingerprint(readString(stream));
				position += Integer.BYTES + fingerprint.size() + Integer.BYTES;
				Chunk chunk = new Chunk(fingerprint, position, stream.readInt());
				position += chunk.size * Long.BYTES;
				stream.skipBytes(chunk.size * Long.BYTES);
				if (!chunks.containsKey(fingerprint)) chunks.put(fingerprint, new ArrayList<>());
				chunks.get(fingerprint).add(chunk);
			}
		} catch (EOFException ignored) {
		}
	}

	private String readString(DataInputStream stream) throws IOException {
		int size = stream.readInt();
		byte[] bytes = new byte[size];
		stream.read(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public void close() {
		file.delete();
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

		public Fingerprint fingerprint() {
			return fingerprint;
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
				return new ZetStream() {
					private DataInputStream inputStream = inputStream();
					private int count = 0;
					private long current = -1;

					@Override
					public long current() {
						return current;
					}

					@Override
					public long next() {
						try {
							if (!hasNext()) return current = -1;
							count++;
							return current = inputStream.readLong();
						} catch (EOFException e) {
							return -1;
						} catch (IOException e) {
							Logger.error(e);
							return -1;
						}
					}

					@Override
					public boolean hasNext() {
						boolean hasNext = count < size;
						if (!hasNext) {
							try {
								inputStream.close();
							} catch (IOException e) {
								Logger.error(e);
							}
						}
						return hasNext;
					}

					private DataInputStream inputStream() throws IOException {
						return new DataInputStream(new ByteArrayInputStream(buffer()));
					}

					private byte[] buffer() throws IOException {
						try (RandomAccessFile access = new RandomAccessFile(file, "r")) {
							byte[] buffer = new byte[Long.BYTES * size];
							access.seek(position);
							access.read(buffer);
							return buffer;
						}
					}
				};
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

	}
}
