package io.intino.ness.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;

import java.io.File;
import java.util.function.Predicate;

public class FileEventTank implements Datalake.EventStore.Tank {
	private final File root;

	FileEventTank(File root) {
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
		return FS.filesIn(root, f -> f.getName().endsWith(FileEventStore.EventExtension))
				.sorted()
				.filter(f -> filter.test(timetagOf(f)))
				.map(ZimReader::new)
				.toArray(ZimStream[]::new);
	}

	private Timetag timetagOf(File file) {
		return new Timetag(file.getName().replace(FileEventStore.EventExtension, ""));
	}
}