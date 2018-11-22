package io.intino.ness.core.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class SetSessionFileWriter {
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

	public void flush()  {
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
			stream.writeInt(fingerprint.size());
			stream.writeBytes(fingerprint.toString());
			stream.writeInt(ids.size());
			Collections.sort(ids);
			for (long id : ids) stream.writeLong(id);
			stream.flush();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void close()  {
		try {
			chunks.forEach(this::write);
			chunks.clear();
			stream.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
