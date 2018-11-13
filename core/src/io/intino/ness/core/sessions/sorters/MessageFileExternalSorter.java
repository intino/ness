package io.intino.ness.core.sessions.sorters;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class MessageFileExternalSorter {
	private static final String ZIM = ".zim";
	private static String SEPARATOR = "\n";
	private final File tankDirectory;
	private final File tempDirectory;
	private File file;

	public MessageFileExternalSorter(File file) {
		this.file = file;
		this.tankDirectory = this.file.getParentFile();
		this.tempDirectory = createTemporalFolder();
	}

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

	private File createTemporalFolder() {
		try {
			return Files.createTempDirectory("externalsorter").toFile();
		} catch (IOException e) {
			Logger.error(e);
			File externalSorter = new File("externalsorter");
			externalSorter.mkdirs();
			return externalSorter;
		}
	}

	public File sort() {
		final ZimStream stream = new ZimReader(file);
		List<File> files = processBatches(stream);
		replace(sortAndMerge(files));
		return file;
	}

	private List<File> processBatches(ZimStream stream) {
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
		File batchFile = new File(tempDirectory, batch + ZIM);
		messages.sort(messageComparator());
		new ZimBuilder(batchFile).put(messages);
		messages.clear();
		return batchFile;
	}


	private File sortAndMerge(List<File> files) {
		File temp = new File(tankDirectory, "temp" + ZIM);
		try (BufferedWriter writer = new BufferedWriter(zipWriter(temp))) {
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

	private OutputStreamWriter zipWriter(File temp) throws IOException {
		return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(temp)));
	}

	private void replace(File temp) {
		if (file.delete()) {
			temp.renameTo(file.getAbsoluteFile());
			deleteDirectory(tempDirectory);
		}
	}

	private TemporalFile temporalFileWithOldestMessage(List<TemporalFile> managers) {
		if (managers.isEmpty()) return null;
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
		return Comparator.comparing(m -> m.asEvent().instant());
	}

	private Instant instantOf(Message message) {
		return message != null ? message.asEvent().instant() : Instant.MAX;
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

