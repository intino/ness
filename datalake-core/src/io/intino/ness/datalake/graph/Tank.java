package io.intino.ness.datalake.graph;

import io.intino.ness.datalake.MessageInputStreamBuilder;
import io.intino.ness.datalake.Scale;
import io.intino.ness.datalake.Sorter;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.intino.ness.datalake.AttachmentLoader.loadAttachments;
import static io.intino.ness.inl.Message.load;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("ALL")
public class Tank extends AbstractTank {
	public static final String INL = ".inl";
	private static final String ZIP = ".zip";
	private static Logger logger = LoggerFactory.getLogger(Tank.class);
	private File currentFile = null;
	private Writer writer = null;
	private boolean batch;
	private int batchBlock;
	private int currentMessageInBlock;
	private Map<String, List<Message>> batchQueue;

	public Tank(io.intino.tara.magritte.Node node) {
		super(node);
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	private static String hourOf(String instant) {
		return dayOf(instant) + instant.substring(instant.indexOf("T") + 1, instant.indexOf(":"));
	}

	private static String monthOf(String instant) {
		return instant.replace("-", "").substring(0, 6);
	}

	public Tank init() {
		directory().mkdirs();
		return this;
	}

	public void batch(int blockSize) {
		this.batch = true;
		this.batchBlock = blockSize;
		this.batchQueue = new HashMap<String, List<Message>>();
	}

	public void endBatch() {
		this.batch = false;
	}

	public void put(String textMessage) {
		Message message;
		try {
			message = load(textMessage);
		} catch (Throwable e) {
			logger.error("error processing message: " + textMessage);
			return;
		}
		put(message);
	}

	public void put(Message message) {
		if (batch) {
			final String key = fileFromInstant(tsOf(message));
			batchQueue.putIfAbsent(key, new ArrayList<>());
			batchQueue.get(key).add(message);
			currentMessageInBlock++;
			if (currentMessageInBlock == batchBlock) {
				saveBatch();
				currentMessageInBlock = 0;
			}
		} else save(message);
	}

	private void saveBatch() {
		for (String ts : batchQueue.keySet()) {
			final List<Message> messages = batchQueue.get(ts);
			if (messages.isEmpty()) continue;
			File file = fileOf(ts);
			if (file.getName().endsWith(ZIP)) file = unzip(file);
			append(file, String.join("\n\n", messages.stream().map(m -> m.toString()).collect(toList())));
			saveAttachments(file, messages);
			flush();
			messages.clear();
		}
	}

	private void save(Message message) {
		File file = destinationFile(message);
		if (file == null) {
			logger.error("impossible to put message:\n " + message.toString());
			return;
		}
		if (file.getName().endsWith(ZIP)) file = unzip(file);
		append(file, message.toString());
		saveAttachments(file, message);
		flush();
	}

	private void append(File file, String text) {
		try {
			if (writer == null || !currentFile.equals(file)) {
				if (writer != null) writer.close();
				if (!file.exists()) file.createNewFile();
				currentFile = file;
				writer = new BufferedWriter(new FileWriter(file, true));
			}
			writer.write(text + "\n\n");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private File unzip(File file) {
		File newFile = new File(file.getName().replace(ZIP, INL));
		try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + file.toURI().toString()), emptyMap())) {
			Files.copy(zipfs.getPath("/" + newFile.getName()), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		file.delete();
		return newFile;
	}

	public void sort() {
		new Sorter(this).sort();
	}

	public void sort(Instant instant) {
		new Sorter(this).sort(instant);
	}

	public void seal() {
		try {
			for (File file : requireNonNull(directory().listFiles((f, n) -> n.endsWith(INL) && !isCurrent(n)))) {
				final byte[] text = readFile(file);
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(file.getAbsolutePath().replace(INL, ZIP))));
				ZipEntry e = new ZipEntry(file.getName());
				out.putNextEntry(e);
				out.write(text, 0, text.length);
				out.closeEntry();
				out.close();
				if (currentFile != null && currentFile.getName().equalsIgnoreCase(file.getName())) {
					currentFile = null;
					writer.close();
				}
				file.delete();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void saveAttachments(File inlFile, List<Message> messages) {
		messages.forEach(message -> saveAttachments(inlFile, message));
	}

	private void saveAttachments(File inlFile, Message message) {
		if (message.attachments().isEmpty()) return;
		try {
			final File directory = attachmentsFolder(inlFile);
			if (!directory.exists()) directory.mkdirs();
			for (Message.Attachment attachment : message.attachments()) {
				File file = new File(directory, attachment.id());
				if (file.exists()) continue;
				Files.write(file.toPath(), attachment.data());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private File attachmentsFolder(File inlFile) {
		return new File(inlFile.getParentFile(), inlFile.getName().replace(INL, ""));
	}

	private boolean isCurrent(String fileName) {
		return (fileFromInstant(Instant.now()) + INL).equals(fileName);
	}

	@Override
	public void delete$() {
		directory().renameTo(new File(graph().core$().as(DatalakeGraph.class).directory(), "old." + qualifiedName()));
		super.delete$();
	}

	public void flush() {
		try {
			if (writer != null) writer.flush();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void terminate() {
		try {
			if (writer != null) writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Iterator<Message> sortedMessagesIterator(Instant from, Instant to) {
		return new SortedMessagesIterator().iterator(from, to);
	}

	public Iterator<Message> sortedMessagesIterator(Map.Entry<Instant, Instant> range) {
		return new SortedMessagesIterator().iterator(range.getKey(), range.getValue());
	}

	public File directory() {
		return new File(graph().directory(), this.qualifiedName());
	}

	public String fileFromInstant(Instant instant) {
		return fileFromInstant(instant.toString());
	}

	private byte[] readFile(File inlFile) throws IOException {
		return Files.readAllBytes(inlFile.toPath());
	}

	private File destinationFile(Message message) {
		if (tsOf(message) == null) return null;
		else {
			final String instant = fileFromInstant(tsOf(message));
			if (currentFile != null && currentFile.getName().equals(instant + INL)) return currentFile;
			return fileOf(instant);
		}
	}

	private File fileOf(String instant) {
		final File tankDirectory = directory();
		return new File(tankDirectory, instant + ZIP).exists() ?
				new File(tankDirectory, instant + ZIP) :
				new File(tankDirectory, instant + INL);
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}

	private String fileFromInstant(String instant) {
		if (graph().scale().equals(Scale.Day)) return dayOf(instant);
		if (graph().scale().equals(Scale.Month)) return monthOf(instant);
		else return hourOf(instant);
	}

	private List<File> filter(List<File> files, Instant from) {
		if (files.isEmpty()) return files;
		final String day = fileFromInstant(from);
		Collections.sort(files);
		final File bound = files.stream().filter(f -> f.getName().equals(day + INL) || f.getName().replace(INL, "").compareTo(day) > 0).findFirst().orElse(null);
		return bound == null || files.isEmpty() ? Collections.emptyList() : files.subList(files.indexOf(bound), files.size());
	}

	public void rename(String name) {
		directory().renameTo(new File(graph().directory(), name));
	}

	public class SortedMessagesIterator {
		public Iterator<Message> iterator(Instant from, Instant to) {
			try {
				File[] files = directory().listFiles((f, n) -> n.endsWith(Tank.INL) || n.endsWith(Tank.ZIP));
				if (files == null) files = new File[0];
				MessageInputStream stream = MessageInputStreamBuilder.of(filter(Arrays.asList(files), from), from);
				return new Iterator<Message>() {
					Message next = stream.next();

					public boolean hasNext() {
						return next != null;
					}

					public Message next() {
						try {
							Message result = next;
							next = stream.next();
							if (next == null) stream.close();
							else if (Instant.parse(tsOf(next)).isAfter(to)) {
								stream.close();
								return next = null;
							} else loadAttachments(new File(stream.name()), next);
							return result;
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
							return null;
						}
					}
				};
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
	}
}