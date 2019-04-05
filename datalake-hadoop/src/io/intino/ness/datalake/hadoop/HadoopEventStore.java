package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datalake.Datalake;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HadoopEventStore implements Datalake.EventStore {
	public static final String EventExtension = ".seq";
	public static final String SessionExtension = ".event.seq";

	private final FileSystem fs;
	private final Path root;

	public HadoopEventStore(FileSystem fs, Path root) {
		this.fs = fs;
		this.root = root;
	}

	@Override
	public Stream<Tank> tanks() {
		try {
			return Arrays.stream(fs.listStatus(root)).
					filter(FileStatus::isDir).
					map(s -> new HadoopEventTank(s.getPath()));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public Tank tank(String name) {
		Path path = new Path(tankPath(name));
		try {
			if (!fs.exists(path)) fs.mkdirs(path);
			return new HadoopEventTank(path);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	void put(ZimStream stream, String blob) {
		while (stream.hasNext()) put(Collections.singletonList(stream.next()), zimFile(blob));
	}

	private void put(List<Message> messages, String tank) {
		try {
			new MessageSequenceWriter(fs, root.toString().replace(fs.getUri().toString(), "") + "/" + tank).write("session_" + UUID.randomUUID(), messages); //TODO change Session
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String tankPath(String tank) {
		return root.toString() + tank;
	}

	private String zimFile(String blob) {
		String fingerprint = blob.substring(0, blob.indexOf("#"));
		return fingerprint.replace("-", "/") + ".zim";
	}
}
