package io.intino.ness.core.local;

import io.intino.ness.core.Datalake;

import java.io.File;
import java.util.stream.Stream;

public class LocalSetStore implements Datalake.SetStore {
	public static final String SetExtension = ".zet";
	public static final String MetadataFilename = ".metadata";

	private final File root;

	public LocalSetStore(File root) {
		this.root = root;
		this.root.mkdirs();
	}

	@Override
	public Stream<Tank> tanks() {
		return FS.foldersIn(root).map(LocalSetTank::new);
	}

	@Override
	public Tank tank(String name) {
		return new LocalSetTank(new File(root, name));
	}


}
