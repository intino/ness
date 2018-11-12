package io.intino.ness.core.fs;

import io.intino.alexandria.zet.Zet;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Datalake.SetStore.Variable;
import io.intino.ness.core.Timetag;

import java.io.File;
import java.util.stream.Stream;

public class FSSet implements Datalake.SetStore.Tank.Tub.Set {
	private final File file;

	public FSSet(File file) {
		this.file = file;
	}

	public String name() {
		return file.getName().replace(FSSetStore.SetExtension, "");
	}

	@Override
	public Timetag timetag() {
		return new Timetag(file.getParentFile().getName());
	}

	public File file() {
		return file;
	}

	@Override
	public int size() {
		return Integer.parseInt(variable("_size_").value);
	}

	@Override
	public ZetStream content() {
		return new ZetReader(file);
	}

	@Override
	public Stream<Variable> variables() {
		return FSMetadata.of(this).map(a -> new Variable(a[1], a[2]));
	}

	@Override
	public Variable variable(String name) {
		return variables().filter(v -> v.name.equals(name)).findFirst().orElse(null);
	}

}
