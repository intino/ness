package io.intino.ness.core.remote;

import org.apache.hadoop.fs.Path;

import static io.intino.ness.core.Datalake.*;

public class Paths {

	public static Path setStorePath(Path root) {
		return new Path(root, SetStoreFolder);
	}

	public static Path eventStorePath(Path root) {
		return new Path(root, EventStoreFolder);
	}

	public static Path stagePath(Path root) {
		return new Path(root, "stage.seq");
	}

	public static Path tempPath(Path root) {
		return new Path(root, "temp");//FIXME
	}

	public static Path sessionsPath(Path root) {
		return new Path(root, SessionsFolder);
	}
}
