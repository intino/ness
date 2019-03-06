package io.intino.ness.core;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZOutputStream;
import io.intino.ness.core.sessions.Fingerprint;
import io.intino.ness.core.sessions.SetSessionWriter;

import java.io.*;
import java.util.*;

public class SetSessionFileWriter implements SetSessionWriter {
	private final Map<Fingerprint, List<Long>> chunks;
	private final DataOutputStream stream;

	public SetSessionFileWriter(OutputStream outputStream) {
		this.chunks = new LinkedHashMap<>();
		this.stream = new DataOutputStream(new BufferedOutputStream(outputStream));
	}

	public void add(String tank, Timetag timetag, String set, long id) {
		check(tank, timetag, set);
		add(Fingerprint.of(tank, timetag, set), id);
	}

	private void check(String tank, Timetag timetag, String set) {
		if (tank == null || tank.isEmpty() || timetag == null || set == null || set.isEmpty())
			throw new RuntimeException("SetStore: tank, timetag or set is not valid or is empty");
	}

	private void add(Fingerprint fingerprint, long id) {
		if (!chunks.containsKey(fingerprint)) chunks.put(fingerprint, new ArrayList<>());
		chunks.get(fingerprint).add(id);
	}

	public void flush() {
		try {
			chunks.forEach(this::write);
			chunks.clear();
			stream.flush();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void write(Fingerprint fingerprint, List<Long> ids) {
		try {
			Collections.sort(ids);
			write(fingerprint.toString().getBytes());
			write(dataOf(ids));
			stream.flush();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void write(byte[] data) throws IOException {
		stream.writeInt(data.length);
		stream.write(data);
	}

	private byte[] dataOf(List<Long> ids) throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		ZOutputStream outputStream = new ZOutputStream(data);
		for (long id : ids) outputStream.writeLong(id);
		outputStream.close();
		return data.toByteArray();
	}

	public void close() {
		try {
			chunks.forEach(this::write);
			chunks.clear();
			stream.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
