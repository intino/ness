package io.intino.ness.core.fs;

import io.intino.ness.core.Datalake;

import java.io.File;
import java.util.stream.Stream;

public class FSSetStore implements Datalake.SetStore {
	public static final String SetExtension = ".zet";
	public static final String MetadataFilename = ".metadata";

	private final File root;

	public FSSetStore(File root) {
		this.root = root;
		this.root.mkdirs();
	}

	@Override
	public Stream<Tank> tanks() {
		return FS.foldersIn(root).map(FSSetTank::new);
	}

	@Override
	public Tank tank(String name) {
		return new FSSetTank(new File(root, name));
	}


}
