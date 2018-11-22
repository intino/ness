package io.intino.ness.core.fs;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake;

import java.io.File;
import java.util.stream.Stream;

public class FSSetTub implements Datalake.SetStore.Tank.Tub {
	private final File root;

	public FSSetTub(File root) {
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
	public Stream<Set> sets() {
		return FS.filesIn(root, f -> f.getName().endsWith(FSSetStore.SetExtension)).map(FSSet::new);
	}

	@Override
	public Stream<Set> sets(Datalake.SetStore.SetFilter filter) {
		return sets().filter(filter);
	}

	@Override
	public Set set(String set) {
		return new FSSet(new File(this.root, set + FSSetStore.SetExtension));
	}

}
