package io.intino.ness.setstore;

import io.intino.ness.setstore.SetStore.Timetag;

import java.util.Iterator;

public class TimetagIterator implements Iterator<Timetag>, Iterable<Timetag> {
	private final Timetag end;
	private Timetag current;

	public TimetagIterator(Timetag startFrom, Timetag end) {
		this.current = startFrom.before();
		this.end = end;
	}

	public boolean hasNext() {
		if (end == null) return true;
		return current.next().toInstant().compareTo(end.toInstant()) < 1;
	}

	public Timetag next() {
		return current = current.next();
	}

	@Override
	public Iterator<Timetag> iterator() {
		return this;
	}
}
