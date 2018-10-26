package io.intino.ness.setstore.session;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

import static io.intino.ness.setstore.file.FileSetStore.SessionExt;
import static io.intino.ness.setstore.file.FileSetStore.TempExt;

public class SessionFileWriter {
	private Map<String, List<Long>> chunks = new LinkedHashMap<>();
	private File file;
	private DataOutputStream stream;

	public SessionFileWriter(File file, Instant instant, boolean append) {
		try {
			file.getParentFile().mkdirs();
			this.file = file;
			this.stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, append)));
			this.stream.writeLong(instant.toEpochMilli());
		} catch (Exception e) {
			e.printStackTrace();
			this.stream = null;
		}
	}

	public void add(String tank, String set, long id) {
		if (tank == null || tank.isEmpty() || set == null || set.isEmpty())
			throw new RuntimeException("Sezzet: tank or set is not valid or is empty");
		String chunkId = chunkId(tank, set);
		if (!chunks.containsKey(chunkId)) chunks.put(chunkId, new ArrayList<>());
		chunks.get(chunkId).add(id);
		// TODO check size and persist chunk
	}

	private String chunkId(String tank, String set) {
		return tank + "@" + set;
	}

	public void flush() throws IOException {
		chunks.forEach(this::write);
		chunks.clear();
		stream.flush();
	}

	private void write(String chunkId, List<Long> ids) {
		try {
			String tank = chunkId.split("@")[0];
			String set = chunkId.split("@")[1];
			Collections.sort(ids);
			stream.writeInt(tank.length());
			stream.writeBytes(tank);
			stream.writeInt(set.length());
			stream.writeBytes(set);
			stream.writeInt(ids.size());
			for (long id : ids) stream.writeLong(id);
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		chunks.forEach(this::write);
		chunks.clear();
		stream.close();
		Files.move(file.toPath(), new File(file.getAbsolutePath().replace(TempExt, SessionExt)).toPath());
	}
}
