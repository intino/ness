package io.intino.ness.setstore.session;


import io.intino.sezzet.operators.SetStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class SessionFileReader {

	private static final int LONG_SIZE = 8;
	private static final int INT_SIZE = 4;
	private final File file;
	private final List<Chunk> chunks = new ArrayList<>();
	private Instant instant;

	public SessionFileReader(File file) throws IOException {
		this.file = file;
		readStructure(file);
	}

	public Instant instant() {
		return instant;
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private void readStructure(File file) throws IOException {
		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			long pos = 8;
			instant = Instant.ofEpochMilli(stream.readLong());
			while (true) {
				String tank = readString(stream);
				String set = readString(stream);
				int idSize = stream.readInt();
				stream.skipBytes(idSize * LONG_SIZE);
				pos += INT_SIZE + tank.length() + INT_SIZE + set.length() + INT_SIZE;
				chunks.add(new Chunk(tank, set, idSize, pos));
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

	public List<Chunk> chunks(String tank, String set) {
		return chunks.stream()
				.filter(c -> c.tank.equals(tank) && c.set.equals(set))
				.collect(toList());
	}

	public class Chunk {
		String tank;
		String set;
		long position;
		private int idSize;

		Chunk(String tank, String set, int idSize, long position) {
			this.tank = tank;
			this.set = set;
			this.idSize = idSize;
			this.position = position;
		}

		public String tank() {
			return tank;
		}

		public String set() {
			return set;
		}

		public SetStream stream() {
			try {
				RandomAccessFile access = new RandomAccessFile(file, "r");
				access.seek(position);
				DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(access.getFD())));
				return new SetStream() {
					int count = 0;
					long current = -1;

					@Override
					public long current() {
						return current;
					}

					@Override
					public long next() {
						try {
							if (!hasNext()) {
								access.close();
								stream.close();
								return current = -1;
							}
							count++;
							return current = stream.readLong();
						} catch (EOFException e) {
							return -1;
						} catch (IOException e) {
							e.printStackTrace();
							return -1;
						}
					}

					@Override
					public boolean hasNext() {
						return count < idSize;
					}
				};
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
