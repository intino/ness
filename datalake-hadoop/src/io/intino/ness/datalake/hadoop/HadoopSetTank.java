package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.Timetag;
import io.intino.ness.datalake.Datalake;
import org.apache.hadoop.fs.Path;

import java.util.stream.Stream;

public class HadoopSetTank implements Datalake.SetStore.Tank {
	private final Path path;

	public HadoopSetTank(Path path) {
		this.path = path;
	}

	@Override
	public String name() {
		return this.path.getName();
	}

	@Override
	public Stream<Datalake.SetStore.Tub> tubs() {
		return null;
	}

	@Override
	public Datalake.SetStore.Tub first() {
		return null;
	}

	@Override
	public Datalake.SetStore.Tub last() {
		return null;
	}

	@Override
	public Datalake.SetStore.Tub on(Timetag tag) {
		return null;
	}

	@Override
	public Stream<Datalake.SetStore.Tub> tubs(int count) {
		return null;
	}

	@Override
	public Stream<Datalake.SetStore.Tub> tubs(Timetag from, Timetag to) {
		return null;
	}

}
