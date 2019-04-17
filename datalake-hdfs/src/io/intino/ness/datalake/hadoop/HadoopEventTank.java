package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datalake.Datalake;
import org.apache.hadoop.fs.Path;

import java.util.function.Predicate;

public class HadoopEventTank implements Datalake.EventStore.Tank {
	private final Path path;

	public HadoopEventTank(Path path) {
		this.path = path;
	}

	@Override
	public String name() {
		return this.path.getName();
	}

	@Override
	public ZimStream content() {
		return ZimStream.Sequence.of(zimStreams(t -> true));
	}

	@Override
	public ZimStream content(Predicate<Timetag> filter) {
		return ZimStream.Sequence.of(zimStreams(filter));
	}

	private ZimStream zimStreams(Predicate<Timetag> filter) {
		return null;
	}
}
