package io.intino.ness.core.fs;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.time.Instant.parse;
import static java.util.stream.Collectors.joining;

public class ExternalSorter {
	private static final String INL = ".inl";
	private static String SEPARATOR = "\n\n";
	private final File tankDirectory;
	private final File tempDirectory;
	private File file;

	public ExternalSorter(File file) {
		this.file = file;
		this.tankDirectory = this.file.getParentFile();
		this.tempDirectory = new File(tankDirectory, this.file.getName().replace(INL, ""));
		tempDirectory.mkdirs();
	}

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

	public File sort() {
		try {
			final ZimStream stream = new ZimReader(file);
			List<File> files = processBatches(stream);
			replace(sortAndMerge(files));
		} catch (IOException e) {
			Logger.error(e);
		}
		return file;
	}

	private List<File> processBatches(ZimStream stream) throws IOException {
		List<File> batches = new ArrayList<>();
		List<Message> current = new ArrayList<>();
		int i = 0;
		int batch = 1;
		while (true) {
			Message next = stream.next();
			if (next == null) break;
			current.add(next);
			i++;
			if (i == 50000) {
				batches.add(processBatch(current, batch++));
				i = 0;
			}
		}
		if (!current.isEmpty()) batches.add(processBatch(current, batch));
		return batches;
	}

	private File processBatch(List<Message> messages, int batch) {
		File inlFile = new File(tempDirectory, batch + INL);
		try {
			messages.sort(messageComparator());
			Files.write(inlFile.toPath(), toString(messages).getBytes(), CREATE, TRUNCATE_EXISTING);
			messages.clear();
		} catch (IOException e) {
			Logger.error(e.getMessage(), e);
		}
		return inlFile;
	}

	private String toString(List<Message> messages) {
		return messages.stream().map(Message::toString).collect(joining(SEPARATOR));
	}

	private File sortAndMerge(List<File> files) {
		File temp = new File(tankDirectory, "temp" + INL);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
			List<TemporalFile> temporalFiles = files.stream().map(TemporalFile::new).collect(Collectors.toList());
			TemporalFile temporalFile = temporalFileWithOldestMessage(temporalFiles);
			while (tempsAreActive(temporalFiles)) {
				writer.write(temporalFile.message.toString() + SEPARATOR);
				temporalFile.next();
				temporalFile = temporalFileWithOldestMessage(temporalFiles);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return temp;
	}

	private void replace(File temp) {
		if (file.delete()) {
			temp.renameTo(file.getAbsoluteFile());
			deleteDirectory(tempDirectory);
		}
	}

	private TemporalFile temporalFileWithOldestMessage(List<TemporalFile> managers) {
		Instant reference = instantOf(managers.get(0).message);
		TemporalFile temporalFile = managers.get(0);
		for (int i = 1; i < managers.size(); i++) {
			Instant comparable = instantOf(managers.get(i).message);
			if (comparable.isBefore(reference)) {
				reference = comparable;
				temporalFile = managers.get(i);
			}
		}
		return temporalFile;
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}

	private Instant instantOf(Message message) {
		return message != null ? parse(tsOf(message)) : Instant.MAX;
	}

	private boolean tempsAreActive(List<TemporalFile> files) {
		for (TemporalFile file : files)
			if (file.message != null) return true;
		return false;
	}

	static class TemporalFile {
		final String source;
		private ZimStream stream;
		private Message message;

		TemporalFile(File file) {
			this.source = file.getName();
			this.stream = new ZimReader(file);
			next();
		}

		private void next() {
			this.message = stream.next();
		}

	}
}

