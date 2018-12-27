package io.intino.ness.setstore;

import java.time.Instant;
import java.util.Iterator;

public class InstantIterator implements Iterator<Instant>, Iterable<Instant> {

	private final Instant end;
	private Instant current;
	private Scale scale;

	public InstantIterator(Instant startFrom, Instant end, Scale scale) {
		this.current = scale.minus(startFrom);
		this.end = end;
		this.scale = scale;
	}

	public boolean hasNext() {
		if (end == null) return true;
		return scale.plus(current).compareTo(end) < 1;
	}

	public Instant next() {
		return current = scale.plus(current);
	}

	@Override
	public Iterator<Instant> iterator() {
		return this;
	}
}
