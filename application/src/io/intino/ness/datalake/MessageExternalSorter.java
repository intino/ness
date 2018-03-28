package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.Instant.parse;

public class MessageExternalSorter {
	private static final String INL = ".inl";
	private static String SEPARATOR = "\n\n";
	private static Logger logger = LoggerFactory.getLogger(MessageExternalSorter.class);

	private File file;
	private final File directory;

	public MessageExternalSorter(File file) {
		this.file = file;
		this.directory = this.file.getParentFile();
	}

	public void sort() {
		try {
			final MessageInputStream stream = FileMessageInputStream.of(file);
			List<File> files = processBatches(stream);
			stream.close();
			sort(files);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<File> processBatches(MessageInputStream stream) throws IOException {
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
		if (!current.isEmpty()) batches.add(processBatch(current, batch++));
		return batches;
	}

	private File processBatch(List<Message> messages, int batch) {
		final File temp = new File(directory, this.file.getName().replace(INL, ""));
		messages.sort(messageComparator());
		temp.mkdirs();
		File inlFile = new File(temp, batch + INL);
		MessageSaver.save(inlFile, messages);
		messages.clear();
		return inlFile;
	}

	private void sort(List<File> files) {
		File temp = new File(directory, "temp" + INL);
		List<TemporalFile> temporalFiles = files.stream().map(TemporalFile::new).collect(Collectors.toList());
		TemporalFile temporalFile = temporalFileWithOldestMessage(temporalFiles);
		while (tempsAreActive(temporalFiles)) {
			write(temp, temporalFile.message);
			temporalFile.next();
			temporalFile = temporalFileWithOldestMessage(temporalFiles);
		}
	}

	private void write(File temp, Message message) {
		try {
			Files.write(temp.toPath(), (message.toString()+ SEPARATOR).getBytes(), CREATE, APPEND);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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

	static class TemporalFile {
		final String source;
		private MessageInputStream iterator;
		private io.intino.ness.inl.Message message;

		TemporalFile(File file) {
			this.source = file.getName();
			try {
				this.iterator = FileMessageInputStream.of(file);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			next();
		}

		private void next() {
			try {
				this.message = iterator.next();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}

	private Instant instantOf(io.intino.ness.inl.Message message) {
		return message != null ? parse(tsOf(message)) : Instant.MAX;
	}


	private boolean tempsAreActive(List<TemporalFile> files) {
		for (TemporalFile file : files)
			if (file.message != null) return true;
		return false;
	}
}
