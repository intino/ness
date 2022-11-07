package io.intino.ness.master.data;

import io.intino.ness.master.data.MasterTripletsDigester.Result.Stats;
import io.intino.ness.master.model.Triplet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileTripletLoader implements TripletLoader {

	private final File root;
	private final Set<String> extensions; // Without .

	public FileTripletLoader(File root) {
		this(root, Set.of("triples", "triplets"));
	}

	public FileTripletLoader(File root, Set<String> extensions) {
		this.root = root;
		this.extensions = extensions;
	}

	@Override
	public Stream<Triplet> loadTriplets(Stats stats) throws IOException {
		return findTripletsFilesIn(root).flatMap(f -> readTripletsFromFile(f, stats));
	}

	protected Stream<Triplet> readTripletsFromFile(File file, Stats stats) {
		stats.increment(Stats.FILES_READ);

		List<Triplet> triplets = new ArrayList<>();

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = reader.readLine()) != null) {
				process(line, triplets, stats);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return triplets.stream();
	}

	protected void process(String line, List<Triplet> triplets, Stats stats) {
		stats.increment(Stats.LINES_READ);
		if(line.isEmpty()) return;
		triplets.add(new Triplet(line));
		stats.increment(Stats.TRIPLETS_READ);
	}

	protected static String extensionOf(File file) {
		String name = file.getName();
		int extensionStart = name.lastIndexOf('.');
		return extensionStart < 0 ? "" : name.substring(extensionStart + 1);
	}

	private Stream<File> findTripletsFilesIn(File root) throws IOException {
		try(Stream<Path> files = Files.walk(root.toPath())) {
			return files.map(Path::toFile)
					.filter(f -> f.isFile() && extensions.contains(extensionOf(f)))
					.collect(Collectors.toList()).stream();
		}
	}
}
