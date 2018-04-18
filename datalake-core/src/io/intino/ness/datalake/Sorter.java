package io.intino.ness.datalake;

import io.intino.konos.alexandria.Inl;
import io.intino.ness.datalake.graph.Tank;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.intino.ness.datalake.graph.Tank.INL;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Objects.requireNonNull;

public class Sorter {
	private static Logger logger = LoggerFactory.getLogger(Sorter.class);

	private Tank tank;

	public Sorter(Tank tank) {
		this.tank = tank;
	}

	public void sort() {
		try {
			tank.flush();
			for (File file : requireNonNull(tank.directory().listFiles((f, n) -> n.endsWith(INL) && !isCurrentFile(n)))) sort(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sort(Instant instant) {
		try {
			final List<File> inlFiles = Arrays.asList(Objects.requireNonNull(tank.directory().listFiles((f, n) -> n.endsWith(INL) && !isCurrentFile(n))));
			for (File file : instant == null ? inlFiles : inlFiles.stream().filter(f -> f.getName().equals(tank.fileFromInstant(instant) + INL)).collect(Collectors.toList()))
				sort(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sort(File file) throws IOException {
		logger.info("sorting " + tank.qualifiedName() + " " + file.getName());
		if (inMb(file.length()) > 50) new MessageExternalSorter(file).sort();
		else {
			final List<Message> messages = loadMessages(file);
			messages.sort(messageComparator());
			overwrite(file, messages);
		}
	}

	private void overwrite(File file, List<Message> messages) {
		try {
			Files.write(file.toPath(), toString(messages).getBytes(), CREATE, TRUNCATE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static String toString(List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		return builder.toString();
	}

	private List<Message> loadMessages(File inlFile) throws IOException {
		return Inl.load(new String(readFile(inlFile), Charset.forName("UTF-8")));
	}


	private byte[] readFile(File inlFile) throws IOException {
		return Files.readAllBytes(inlFile.toPath());
	}

	private long inMb(long length) {
		return length / (1024 * 1024);
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> {
			final String text = m.get("ts");
			if (text == null) logger.error("ts is null for message: " + m.toString());
			return Instant.parse(text);
		});
	}

	private boolean isCurrentFile(String file) {
		return (tank.fileFromInstant(Instant.now()) + INL).equals(file);
	}

}
