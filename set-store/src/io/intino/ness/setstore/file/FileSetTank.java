package io.intino.ness.setstore.file;

import io.intino.konos.TripleStore;
import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.SetStore.Tank.Tub.Set;
import io.intino.ness.setstore.SetStore.Timetag;
import io.intino.ness.setstore.TimetagIterator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	@Override
	public Tub first() {
		List<File> files = files();
		if (files.isEmpty()) return null;
		return new FileSetTub(new File(directory, files.get(0).getName()), null, this);
	}

	@Override
	public Tub last() {
		List<File> files = files();
		if (files.isEmpty()) return null;
		return new FileSetTub(new File(directory, files.get(files.size() - 1).getName()), null, this);
	}

	public Tub on(Timetag tag) {
		return new FileSetTub(new File(directory, tag.toString()),tag,this);
	}

	@Override
	public List<Tub> tubs() {
		return files().stream().map(f -> new FileSetTub(f, null, this)).collect(Collectors.toList());
	}

	private List<File> files() {
		File[] files = directory.listFiles(File::isDirectory);
		if (files == null) return Collections.emptyList();
		Arrays.sort(files);
		return Arrays.asList(files);
	}

	@Override
	public Stream<Tub> tubs(int count) {
		return null;
	}

	@Override
	public List<Tub> tubs(Timetag from, Timetag to) {
		List<Tub> tubs = new ArrayList<>();
		for (Timetag timetag : new TimetagIterator(from, to))
			tubs.add(on(timetag));
		return tubs;
	}

	@Override
	public List<Set> setsOf(Timetag from, Timetag to) {
		java.util.Set<Set> sets = new LinkedHashSet<>();
		for (Timetag timetag : new TimetagIterator(from, to))
			sets.addAll(on(timetag).sets());
		return new ArrayList<>(sets);
	}

	@Override
	public List<Set> setsOf(Timetag from, Timetag to, SetStore.SetFilter filter) {
		return setsOf(from, to).stream().filter(filter).collect(Collectors.toList());
	}
}
