package io.intino.ness.master.persistence;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.master.model.Triplet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;

public class MasterTripletWriter {

	private static final String TRIPLETS_EXTENSION = ".triplets";

	private final File datalakeEntitiesPath;

	public MasterTripletWriter(File datalakeEntitiesPath) {
		this.datalakeEntitiesPath = requireNonNull(datalakeEntitiesPath);
	}

	public void write(String tank, List<Triplet> triplets) throws IOException {
		synchronized(MasterTripletWriter.class) {
			File tankDir = new File(datalakeEntitiesPath, capitalize(tank));
			tankDir.mkdirs();

			File file = new File(tankDir, todaysTimetag() + TRIPLETS_EXTENSION);
			File tmp = new File(file.getAbsolutePath() + "_" + System.nanoTime() + ".tmp");
			tmp.deleteOnExit();

			try {
				if (file.exists()) Files.copy(file.toPath(), tmp.toPath(), REPLACE_EXISTING);
				Files.write(tmp.toPath(), serialize(triplets), CREATE, WRITE, APPEND);
				Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
			} finally {
				tmp.delete();
			}
		}
	}

	private String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private Iterable<String> serialize(List<Triplet> triplets) {
		List<String> lines = triplets.stream().map(Triplet::toString).collect(Collectors.toList());
		lines.add("");
		return lines;
	}

	private static Iterable<Map.Entry<String, List<Triplet>>> groupByType(List<Triplet> triplets) {
		return triplets.stream().collect(Collectors.groupingBy(Triplet::type)).entrySet();
	}

	private String todaysTimetag() {
		return Timetag.of(LocalDate.now(), Scale.Day).value();
	}
}
