package io.intino.ness.core.fs;

import io.intino.ness.core.Datalake;
import io.intino.ness.core.Scale;
import io.intino.ness.core.Timetag;

import java.io.File;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FSSetTank implements Datalake.SetStore.Tank {
	private final File root;

	public FSSetTank(File root) {
		this.root = root;
	}

	@Override
	public String name() {
		return root.getName();
	}

	@Override
	public Tub first() {
		return tubs().findFirst().orElse(currentTub());
	}

	@Override
	public Tub last() {
		return FS.foldersIn(root, FS.Sort.Reversed).map(FSSetTub::new).findFirst().orElse(currentTub());
	}

	public Tub on(Timetag tag) {
		return new FSSetTub(new File(root, tag.value()));
	}

	@Override
	public Stream<Tub> tubs() {
		return FS.foldersIn(root).map(FSSetTub::new);
	}

	@Override
	public Stream<Tub> tubs(int count) {
		return FS.foldersIn(root, FS.Sort.Reversed).map(f -> (Tub) new FSSetTub(f)).limit(count);
	}

	@Override
	public Stream<Tub> tubs(Timetag from, Timetag to) {
		return StreamSupport.stream(from.iterateTo(to).spliterator(), false).map(this::on);
	}

	private FSSetTub currentTub() {
		return new FSSetTub(new File(root, new Timetag(LocalDateTime.now(), Scale.Month).toString()));
	}

}
