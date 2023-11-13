package io.intino.datahub.model;

import io.intino.datahub.model.Indicator.Shot;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.ofEpochMilli;

public class IndicatorFile {
	private final File file;

	public static IndicatorFile of(File file) {
		return new IndicatorFile(file);
	}

	public IndicatorFile(File file) {
		this.file = file;
	}

	public IndicatorFile(String file) {
		this(new File(file));
	}

	public File file() {
		return file;
	}

	public Indicator get() throws IOException {
		Map<String, Shot> shots;
		if (!file.exists()) return new Indicator(new HashMap<>());
		try (var stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			int size = stream.readInt();
			shots = new HashMap<>(size);
			for (int i = 0; i < size; i++)
				shots.put(stream.readUTF(), new Shot(ofEpochMilli(stream.readLong()), stream.readDouble()));
		}
		return new Indicator(shots);
	}

	public void save(Indicator indicator) throws IOException {
		try (var stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			stream.writeInt(indicator.shots().size());
			for (Map.Entry<String, Shot> entry : indicator.shots().entrySet()) {
				stream.writeUTF(entry.getKey());
				stream.writeLong(entry.getValue().ts().toEpochMilli());
				stream.writeDouble(entry.getValue().value());
			}
		}
	}
}
