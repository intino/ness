package io.intino.master.io;

import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TriplesFileReader {

	private final File folder;

	public TriplesFileReader(File folder) {
		this.folder = folder;
	}

	private Triple tripleOf(String line) {
		return new Triple(line);
	}

	private Stream<String> linesOf(Path path) {
		try {
			return Files.readAllLines(path).stream();
		} catch (IOException e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	public List<Triple> triples() {
		try (Stream<Path> stream = Files.walk(folder.toPath())) {
			return stream.filter(f -> f.toFile().getName().endsWith(".triples"))
					.flatMap(this::linesOf)
					.filter(l -> !l.trim().isEmpty())
					.map(this::tripleOf)
					.collect(Collectors.toList());
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}
}
