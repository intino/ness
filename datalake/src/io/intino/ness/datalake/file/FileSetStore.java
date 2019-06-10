package io.intino.ness.datalake.file;

import io.intino.ness.datalake.Datalake;

import java.io.File;
import java.util.stream.Stream;

import static io.intino.alexandria.zim.ZimReader.ZimExtension;

public class FileSetStore implements Datalake.SetStore {
	public static final String SetExtension = ".zet";
	public static final String MetadataFilename = ".metadata";
	public static final String IndexFileName = ".mapp";


	private final File root;

	public FileSetStore(File root) {
		this.root = root;
		this.root.mkdirs();
	}

	@Override
	public Stream<Tank> tanks() {
		return FS.foldersIn(root).map(FileSetTank::new);
	}

	@Override
	public Tank tank(String name) {
		return new FileSetTank(new File(root, name));
	}


}
