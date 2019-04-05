package io.intino.ness.core.hadoop;

import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake;
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
	public Stream<Tub> tubs() {
		return null;
	}

	@Override
	public Tub first() {
		return null;
	}

	@Override
	public Tub last() {
		return null;
	}

	@Override
	public Tub on(Timetag tag) {
		return null;
	}

	@Override
	public Stream<Tub> tubs(int count) {
		return null;
	}

	@Override
	public Stream<Tub> tubs(Timetag from, Timetag to) {
		return null;
	}

}
