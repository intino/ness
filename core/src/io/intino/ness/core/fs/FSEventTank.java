package io.intino.ness.core.fs;

import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Timetag;

import java.io.File;
import java.util.function.Predicate;

import static io.intino.ness.core.fs.FSEventStore.EventExtension;

public class FSEventTank implements Datalake.EventStore.Tank {
	private final File root;

	FSEventTank(File root) {
		this.root = root;
	}

	@Override
	public String name() {
		return root.getName();
	}

	@Override
	public ZimStream content() {
		return ZimStream.Sequence.of(zimStreams(t -> true));
	}

	@Override
	public ZimStream content(Predicate<Timetag> filter) {
		return ZimStream.Sequence.of(zimStreams(filter));

	}

	private ZimStream[] zimStreams(Predicate<Timetag> filter) {
		return FS.filesIn(root, f -> f.getName().endsWith(EventExtension))
				.filter(f -> filter.test(timetagOf(f)))
				.map(ZimReader::new)
				.toArray(ZimStream[]::new);
	}

	private Timetag timetagOf(File file) {
		return new Timetag(file.getName().replace(EventExtension, ""));
	}

}
