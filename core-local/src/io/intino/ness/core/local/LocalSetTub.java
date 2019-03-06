package io.intino.ness.core.local;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake;

import java.io.File;
import java.util.stream.Stream;

public class LocalSetTub implements Datalake.SetStore.Tank.Tub {
	private final File root;

	public LocalSetTub(File root) {
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
		return FS.filesIn(root, f -> f.getName().endsWith(LocalSetStore.SetExtension)).map(LocalSet::new);
	}

	@Override
	public Stream<Set> sets(Datalake.SetStore.SetFilter filter) {
		return sets().filter(filter);
	}

	@Override
	public Set set(String set) {
		return new LocalSet(new File(this.root, set + LocalSetStore.SetExtension));
	}

}
