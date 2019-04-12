package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.datalake.Datalake;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HadoopDatalake implements Datalake {
	private final FileSystem fs;
	private final Path root;

	public HadoopDatalake(FileSystem fs) {
		this.root = new Path(fs.getWorkingDirectory(), "datalake");
		this.fs = fs;
		mkdirs();
	}

	@Override
	public EventStore eventStore() {
		return new HadoopEventStore(fs, eventStorePath());
	}

	@Override
	public SetStore setStore() {
		return new HadoopSetStore(fs, setStorePath());
	}

	Path stagePath() {
		return stagePath(this.root);
	}

	Path eventStorePath() {
		return eventStorePath(root);
	}

	Path setStorePath() {
		return setStorePath(root);
	}

	private void mkdirs() {
		try {
			if (!fs.exists(root)) fs.mkdirs(root);
			if (!fs.exists(stagePath(root))) fs.mkdirs(stagePath(root));
			if (!fs.exists(setStorePath(root))) fs.mkdirs(setStorePath(root));
			if (!fs.exists(eventStorePath(root))) fs.mkdirs(eventStorePath(root));
		} catch (IOException e) {
			Logger.error(e);
		}
	}


	private Path setStorePath(Path root) {
		return new Path(root, SetStoreFolder);
	}

	private Path eventStorePath(Path root) {
		return new Path(root, EventStoreFolder);
	}

	private Path stagePath(Path root) {
		return new Path(root, "stage.seq");
	}
}
