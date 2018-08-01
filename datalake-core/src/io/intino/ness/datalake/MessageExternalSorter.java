package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.time.Instant.parse;

public class MessageExternalSorter {
	private static final String INL = ".inl";
	private static String SEPARATOR = "\n\n";
	private static Logger logger = LoggerFactory.getLogger(MessageExternalSorter.class);
	private final File tankDirectory;
	private final File tempDirectory;
	private File file;

	MessageExternalSorter(File file) {
		this.file = file;
		this.tankDirectory = this.file.getParentFile();
		this.tempDirectory = new File(tankDirectory, this.file.getName().replace(INL, ""));
		tempDirectory.mkdirs();
	}

	public void sort() {
		try {
			final MessageInputStream stream = FileMessageInputStream.of(file);
			List<File> files = processBatches(stream);
			stream.close();
			replace(sortAndMerge(files));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<File> processBatches(MessageInputStream stream) throws IOException {
		List<File> batches = new ArrayList<>();
		List<Message> current = new ArrayList<>();
		int batch = 1;
		int bytes = 0;
		while (true) {
			Message next = stream.next();
			if (next == null) break;
			current.add(next);
			bytes += next.toString().length();
			if (inMb(bytes) >= 50) {
				batches.add(processBatch(current, batch++));
				bytes = 0;
			}
		}
		if (!current.isEmpty()) batches.add(processBatch(current, batch++));
		return batches;
	}

	private File processBatch(List<Message> messages, int batch) {
		File inlFile = new File(tempDirectory, batch + INL);
		try {
			TimSort sorter = new TimSort();
			final Message[] list = messages.toArray(new Message[0]);
			sorter.doSort(list, messageComparator());
			Files.write(inlFile.toPath(), toString(messages).getBytes(), CREATE, TRUNCATE_EXISTING);
			messages.clear();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return inlFile;
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
			logger.error(e.getMessage(), e);
		}
		return temp;
	}

	private void replace(File temp) {
		if (file.delete()) {
			temp.renameTo(file.getAbsoluteFile());
			try {
				FileUtils.deleteDirectory(tempDirectory);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
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

	private static Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private static String tsOf(Message message) {
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
		private MessageInputStream stream;
		private Message message;

		TemporalFile(File file) {
			this.source = file.getName();
			try {
				this.stream = FileMessageInputStream.of(file);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			next();
		}

		private void next() {
			try {
				this.message = stream.next();
				if (message != null) AttachmentLoader.loadAttachments(new File(stream.name()), this.message);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static String toString(List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append(SEPARATOR);
		return builder.toString();
	}

	public static String toString(Message[] messages) {
		return String.join(SEPARATOR, Arrays.stream(messages).map(Message::toString).toArray(String[]::new));
	}

	private long inMb(long length) {
		return length / (1024 * 1024);
	}
}
