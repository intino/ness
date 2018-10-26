package io.intino.ness.setstore.file;

import io.intino.konos.TripleStore;
import io.intino.ness.setstore.InstantIterator;
import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.SetStore.Tank.Tub.Set;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class FileSetTank implements SetStore.Tank {
	private final File directory;
	private final Scale scale;
	private static final int MAX_ENTRIES = 1000;
	static Map<String, TripleStore> tripleStoreMap = new LinkedHashMap<String, TripleStore>(MAX_ENTRIES + 1, .75F, true) {
		public boolean removeEldestEntry(Map.Entry eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	public FileSetTank(File directory, Scale scale) {
		this.directory = directory;
		this.scale = scale;
	}

	@Override
	public String name() {
		return directory.getName();
	}

	Scale scale() {
		return scale;
	}

	public Tub tub(Instant instant) {
		return new FileSetTub(new File(directory, scale.tag(instant)), instant, this);
	}

	@Override
	public List<Tub> tubs(Instant from, Instant to) {
		List<Tub> tubs = new ArrayList<>();
		for (Instant instant : new InstantIterator(from, to, scale))
			tubs.add(tub(instant));
		return tubs;
	}

	@Override
	public List<Set> setsOf(Instant from, Instant to) {
		java.util.Set<Set> sets = new LinkedHashSet<>();
		for (Instant instant : new InstantIterator(from, to, scale))
			sets.addAll(tub(instant).sets());
		return new ArrayList<>(sets);
	}

	@Override
	public List<Set> setsOf(Instant from, Instant to, SetStore.SetFilter filter) {
		return setsOf(from, to).stream().filter(filter).collect(Collectors.toList());
	}
}
