package io.intino.ness.core.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SetSessionFileReader {

	private static final int LONG_SIZE = 8;
	private static final int INT_SIZE = 4;
	private final File file;
	private final List<Chunk> chunks = new ArrayList<>();

	public SetSessionFileReader(File file) throws IOException {
		this.file = unzip(file);
		this.readStructure(this.file);
	}

	public Stream<Chunk> chunks() {
		return chunks.stream();
	}

	public Stream<Chunk> chunks(Fingerprint fingerprint) {
		return chunks.stream()
				.filter(c -> c.fingerprint.equals(fingerprint));
	}

	private File unzip(File file) throws IOException {
		File tempFile = tempFile();
		InputStream stream = inputStreamOf(file);
		Files.copy(stream, tempFile.toPath(), REPLACE_EXISTING);
		stream.close();
		return tempFile;
	}

	private File tempFile() throws IOException {
		return File.createTempFile("blob", "blob");
	}

	private GZIPInputStream inputStreamOf(File file) throws IOException {
		return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private void readStructure(File file) throws IOException {
		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			long pos = 0;
			while (true) {
				Fingerprint fingerprint = new Fingerprint(readString(stream));
				int idSize = stream.readInt();
				stream.skipBytes(idSize * LONG_SIZE);
				pos += INT_SIZE + fingerprint.size() + INT_SIZE;
				chunks.add(new Chunk(fingerprint, idSize, pos));
				pos += idSize * LONG_SIZE;
			}
		} catch (EOFException ignored) {
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
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
		long position;
		private int idSize;

		Chunk(Fingerprint fingerprint, int idSize, long position) {
			this.fingerprint = fingerprint;
			this.idSize = idSize;
			this.position = position;
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
				RandomAccessFile access = new RandomAccessFile(file, "r");
				access.seek(position);
				DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(access.getFD())));
				return new ZetStream() {
					int count = 0;
					long current = -1;

					@Override
					public long current() {
						return current;
					}

					@Override
					public long next() {
						try {
							if (!hasNext()) return current = -1;
							count++;
							return current = stream.readLong();
						} catch (EOFException e) {
							return -1;
						} catch (IOException e) {
							Logger.error(e);
							return -1;
						}
					}

					@Override
					public boolean hasNext() {
						boolean hasNext = count < idSize;
						if (!hasNext) {
							try {
								access.close();
								stream.close();
							} catch (IOException e) {
								Logger.error(e);
							}
						}
						return hasNext;
					}
				};
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

	}
}
