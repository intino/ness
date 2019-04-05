package io.intino.ness.datalake.file;

import io.intino.ness.datalake.Datalake;

import java.io.File;
import java.util.stream.Stream;

public class FileSetIndex implements Datalake.SetStore.Index {
	private final File root;

	public FileSetIndex(File root) {
		this.root = root;
	}

	@Override
	public String find(long id) {
		return null;
	}

	@Override
	public Stream<Entry> entries() {
		return null;
	}
}
