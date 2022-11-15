package io.intino.ness.master.data.update;

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

public class MasterPublisher {

	private static final String TRIPLETS_EXTENSION = ".triplets";

	private final File datalakeTripletsPath;

	public MasterPublisher(File datalakeTripletsPath) {
		this.datalakeTripletsPath = requireNonNull(datalakeTripletsPath);
	}

	public void publish(List<Triplet> triplets) throws IOException {
		synchronized (MasterPublisher.class) {
			for (Map.Entry<String, List<Triplet>> entry : groupByType(triplets)) {
				publish(entry.getKey(), entry.getValue());
			}
		}
	}

	private void publish(String tank, List<Triplet> triplets) throws IOException {
		File tankDir = new File(datalakeTripletsPath, capitalize(tank));
		tankDir.mkdirs();

		File file = new File(tankDir, todaysTimetag() + TRIPLETS_EXTENSION);
		File tmp = new File(file.getAbsolutePath() + "_" + System.nanoTime() + ".tmp");
		tmp.deleteOnExit();

		try {
			Files.copy(file.toPath(), tmp.toPath(), REPLACE_EXISTING);
			Files.write(tmp.toPath(), serialize(triplets), CREATE, WRITE, APPEND);
			Files.move(tmp.toPath(), file.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
		} finally {
			tmp.delete();
		}
	}

	private String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private Iterable<String> serialize(List<Triplet> triplets) {
		return triplets.stream().map(Triplet::toString).collect(Collectors.toList());
	}

	private static Iterable<Map.Entry<String, List<Triplet>>> groupByType(List<Triplet> triplets) {
		return triplets.stream().collect(Collectors.groupingBy(Triplet::type)).entrySet();
	}

	private String todaysTimetag() {
		return Timetag.of(LocalDate.now(), Scale.Day).value();
	}
}
