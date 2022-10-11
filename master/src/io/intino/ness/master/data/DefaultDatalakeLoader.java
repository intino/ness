package io.intino.ness.master.data;

import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;

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

	public static final String TRIPLETS_EXTENSION = ".triples";

	@Override
	public LoadResult load(File rootDirectory, MasterSerializer serializer) {
		WritableLoadResult result = LoadResult.create();
		loadRecordsFromDisk(rootDirectory, result, serializer);
		return result;
	}

	protected void loadRecordsFromDisk(File rootDirectory, WritableLoadResult result, MasterSerializer serializer) {
		Map<String, TripletRecord> records = result.records();
		try(Stream<Path> files = Files.walk(rootDirectory.toPath())) {
			files.map(Path::toFile)
					.filter(f -> f.isFile() && f.getName().endsWith(TRIPLETS_EXTENSION))
					.flatMap(file -> readTripletsFromFile(file, result))
					.forEach(t -> records.computeIfAbsent(t.subject(), TripletRecord::new).put(t));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Stream<Triplet> readTripletsFromFile(File file, WritableLoadResult result) {
		result.filesRead().add(file);

		List<Triplet> triplets = new ArrayList<>();

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while((line = reader.readLine()) != null) {
				process(line, triplets, result);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return triplets.stream();
	}

	protected void process(String line, List<Triplet> triplets, WritableLoadResult result) {
		result.linesRead(result.linesRead() + 1);
		if(line.isEmpty()) return;
		triplets.add(new Triplet(line));
		result.triplesRead(result.triplesRead() + 1);
	}
}
