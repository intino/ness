package io.intino.ness.core.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Timetag;
import io.intino.alexandria.zet.ZetStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SetSessionFileReader {

	private static final int LONG_SIZE = 8;
	private static final int INT_SIZE = 4;
	private final File file;
	private final List<Chunk> chunks = new ArrayList<>();

	public SetSessionFileReader(File file) throws IOException {
		this.file = file;
		readStructure(file);
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

	public List<Chunk> chunks() {
		return chunks;
	}

	public List<Chunk> chunks(Fingerprint fingerprint) {
		return chunks.stream()
				.filter(c -> c.fingerprint.equals(fingerprint))
				.collect(toList());
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
