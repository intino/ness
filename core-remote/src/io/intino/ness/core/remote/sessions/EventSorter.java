package io.intino.ness.core.remote.sessions;

import io.intino.alexandria.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.time.Instant.parse;

public class EventSorter {
	private final File file;
	private final File temp;
	private final List<long[]> tuples;

	public EventSorter(File file, File tempFolder) throws IOException {
		this.file = file;
		this.temp = File.createTempFile("event", ".inl", tempFolder);
		this.tuples = new ArrayList<>();
	}

	void sort() throws IOException {
		sort(file);
	}

	void sort(File destination) throws IOException {
		try {
			read();
			tuples.sort(Comparator.comparing(t -> t[0]));
			write(outputStream(destination));
			Files.delete(temp.toPath());
		} catch (IOException io) {
			if (temp.exists()) {
				Logger.warn("Deleting inl temporal file " + file.getAbsolutePath());
				Files.delete(temp.toPath());
			}
			throw io;
		}
	}

	private void read() throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream())); BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
			int offset = 0;
			int size = 0;
			Instant instant = null;
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (isTS(line)) instant = parse(line.substring(line.indexOf(":") + 1).trim());
				else if (isMainHeader(line)) {
					addTuple(instant, offset, size);
					offset += size;
					size = 0;
				}
				size += line.getBytes().length + 1;
				writer.write(line + "\n");
			}
			addTuple(instant, offset, size);
		}
	}

	private void write(OutputStream output) throws IOException {
		try (RandomAccessFile input = new RandomAccessFile(temp, "r")) {
			for (long[] t : tuples) write(output, bytesOf(input, t));
			output.flush();
			output.close();
		}
	}

	private void write(OutputStream output, byte[] bytes) {
		try {
			output.write(bytes);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private OutputStream outputStream() throws IOException {
		return new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)), true);
	}

	private OutputStream outputStream(File file) throws IOException {
		return new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	}

	private byte[] bytesOf(RandomAccessFile accessFile, long[] tuple) {
		try {
			return read(accessFile, (int) (tuple[1] >> 32), new byte[(int) tuple[1]]);
		} catch (IOException e) {
			Logger.error(e);
			return new byte[0];
		}
	}

	private byte[] read(RandomAccessFile accessFile, int offset, byte[] buffer) throws IOException {
		accessFile.seek(offset);
		accessFile.read(buffer);
		return buffer;
	}

	private void addTuple(Instant instant, int offset, int size) {
		if (instant == null) return;
		tuples.add(new long[]{instant.toEpochMilli(), (((long) offset) << 32) + ((long) size)});
	}

	private boolean isTS(String line) {
		return line.startsWith("ts");
	}

	private boolean isMainHeader(String line) {
		return line.startsWith("[") && !line.contains(".");
	}

	private InputStream inputStream() throws IOException {
		return new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));

	}
}
