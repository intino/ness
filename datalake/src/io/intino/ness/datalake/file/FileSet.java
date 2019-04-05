package io.intino.ness.datalake.file;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZFile;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.Datalake.SetStore.Variable;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public class FileSet implements Datalake.SetStore.Set {
	private final File file;

	public FileSet(File file) {
		this.file = file;
	}

	public String name() {
		return file.getName().replace(FileSetStore.SetExtension, "");
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
			return file.exists() ? (int) new ZFile(file).size() : 0;
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
		return FileMetadata.of(this).map(a -> new Variable(a[1], a[2]));
	}

	@Override
	public Variable variable(String name) {
		return variables().filter(v -> v.name.equals(name)).findFirst().orElse(null);
	}

}
