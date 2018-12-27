package io.intino.ness.setstore.file;

import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.session.SessionFileWriter;
import io.intino.ness.setstore.session.SessionSealer;
import io.intino.sezzet.operators.SetStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class FileSetStore implements SetStore {

	public static final String SessionExt = ".session";
	public static final String SegmentExt = ".segment";
	public static final String InfoExt = ".info";
	public static final String SetExt = ".set";
	public static final String TempExt = ".temp";
	public static final String PartExt = ".part";

	private final File store;
	private Scale scale;

	public FileSetStore(File store, Scale scale) {
		this.store = store;
		this.scale = scale;
	}

	public static void write(SetStream stream, File file) throws IOException {
		file.getParentFile().mkdirs();
		DataOutputStream dataStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		while (stream.hasNext()) dataStream.writeLong(stream.next());
		dataStream.close();
	}

	@Override
	public Scale scale() {
		return scale;
	}


	//TODO old
	public SessionFileWriter createSession(Instant instant) {
		return new SessionFileWriter(sessionFile(instant), instant, false);
	}

	//TODO old
	public void seal() {
		SessionSealer.seal(stageFolder());
	}


	public File storeSegment(String segment, Timetag timetag, SetStream stream) {
		try {
			File file = new File(segmentFolder(), timetag.toString() + "/" + segment + SegmentExt);
			write(stream, file);
			return file;
		} catch (IOException e) {
			LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public List<Tank> tanks() {
		File[] tanks = store.listFiles((dir, name) -> !name.equals("stage"));
		if (tanks == null) return emptyList();
		return Arrays.stream(tanks).map(directory -> new FileSetTank(directory, scale)).collect(toList());
	}

	@Override
	public Tank tank(String name) {
		File[] tanks = store.listFiles((dir, n) -> !n.equals("stage"));
		if (tanks == null) return null;
		File file = Arrays.stream(tanks).filter(f -> f.getName().equals(name)).findFirst().orElse(null);
		return file == null ? null : new FileSetTank(file, scale);
	}


	private File sessionFile(Instant instant) {
		int count = -1;
		while (true) {
			File result = new File(stageFolder(), scale.tag(instant) + PartExt + ++count);
			if (!new File(result + TempExt).exists() && !new File(result + SessionExt).exists())
				return new File(result + TempExt);
		}
	}

	private File segmentFolder() {
		return new File(store, "segments/");
	}

	private File stageFolder() {
		return new File(store, "stage/");
	}
}
