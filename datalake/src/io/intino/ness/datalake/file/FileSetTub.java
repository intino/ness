package io.intino.ness.datalake.file;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.mapp.MappReader;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.Datalake.SetStore.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.intino.ness.datalake.file.FileSetStore.IndexFileName;
import static io.intino.ness.datalake.file.FileSetStore.SetExtension;

public class FileSetTub implements Datalake.SetStore.Tub {
	private final File root;

	public FileSetTub(File root) {
		this.root = root;
	}

	public String name() {
		return root.getName();
	}

	@Override
	public Timetag timetag() {
		return new Timetag(name());
	}

	@Override
	public Scale scale() {
		return timetag().scale();
	}

	@Override
	public MappReader index() {
		try {
			if (!indexFile().exists()) return null;
			return new MappReader(this.root.getParentFile().getName() + "-" + name(), new FileInputStream(indexFile()));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private File indexFile() {
		return new File(root, IndexFileName);
	}

	@Override
	public Set set(String set) {
		return new FileSet(new File(this.root, set + SetExtension));
	}

	@Override
	public Stream<Set> sets() {
		return FS.filesIn(root, f -> f.getName().endsWith(SetExtension)).map(FileSet::new);
	}

	@Override
	public Stream<Set> sets(Predicate<Set> filter) {
		return sets().filter(filter);
	}
}
