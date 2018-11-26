package io.intino.ness.core.fs;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZFile;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Datalake.SetStore.Variable;

import java.io.File;
import java.io.IOException;
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
		try {
			return (int) new ZFile(file).size();
		} catch (IOException e) {
			Logger.error(e);
			return 0;
		}
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
