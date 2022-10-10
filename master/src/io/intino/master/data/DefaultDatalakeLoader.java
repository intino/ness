package io.intino.master.data;

import io.intino.master.model.Triple;
import io.intino.master.model.TripleRecord;
import io.intino.master.serialization.MasterSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DefaultDatalakeLoader implements DatalakeLoader {

	public static final String TRIPLES_EXTENSION = ".triples";

	@Override
	public LoadResult load(File rootDirectory, MasterSerializer serializer) {
		WritableLoadResult result = LoadResult.create();
		loadRecordsFromDisk(rootDirectory, result, serializer);
		return result;
	}

	protected void loadRecordsFromDisk(File rootDirectory, WritableLoadResult result, MasterSerializer serializer) {
		Map<String, TripleRecord> records = result.records();
		try(Stream<Path> files = Files.walk(rootDirectory.toPath())) {
			files.map(Path::toFile)
					.filter(f -> f.isFile() && f.getName().endsWith(TRIPLES_EXTENSION))
					.flatMap(file -> readTriplesFromFile(file, result))
					.forEach(triple -> records.computeIfAbsent(triple.subject(), TripleRecord::new).setAttribute(triple.predicate(), triple.value()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Stream<Triple> readTriplesFromFile(File file, WritableLoadResult result) {
		result.filesRead().add(file);

		List<Triple> triples = new ArrayList<>();

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = reader.readLine()) != null) {
				process(line, triples, result);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return triples.stream();
	}

	protected void process(String line, List<Triple> triples, WritableLoadResult result) {
		result.linesRead(result.linesRead() + 1);
		if(line.isEmpty()) return;
		triples.add(new Triple(line));
		result.triplesRead(result.triplesRead() + 1);
	}
}
