package io.intino.ness.datalake;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.datalake.Datalake.SetStore.Index;
import io.intino.ness.datalake.Datalake.SetStore.Set;

import java.io.File;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
	public Index index() {
		return new FileSetIndex(root);
	}

	@Override
	public Set set(String set) {
		//TODO
		return null;
	}

	@Override
	public Stream<Set> sets() {
		return FS.filesIn(root, f -> f.getName().endsWith(FileSetStore.SetExtension)).map(FileSet::new);
	}

	@Override
	public Stream<Set> sets(Predicate<Set> filter) {
		return sets().filter(filter);
	}
}
