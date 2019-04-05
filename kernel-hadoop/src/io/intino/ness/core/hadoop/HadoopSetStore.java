package io.intino.ness.core.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Datalake;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class HadoopSetStore implements Datalake.SetStore {
	public static final String SetExtension = ".seq";
	public static final String MetadataFilename = ".metadata";
	private final FileSystem fs;
	private final Path root;

	public HadoopSetStore(FileSystem fs, Path root) {
		this.fs = fs;
		this.root = root;
	}

	@Override
	public Stream<Tank> tanks() {
		try {
			return Arrays.stream(fs.listStatus(root)).
					filter(FileStatus::isDir).
					map(s -> new HadoopSetTank(s.getPath()));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}


	@Override
	public Tank tank(String name) {
		return null;
	}

	public void put(ZetStream stream, String name) {
		try {
			new SetSequenceWriter(fs, name).write(stream);
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
