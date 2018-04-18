package io.intino.ness.datalake.graph;

import io.intino.ness.datalake.MessageInputStreamBuilder;
import io.intino.ness.datalake.Scale;
import io.intino.ness.datalake.Sorter;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.intino.ness.inl.Message.load;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("ALL")
public class Tank extends AbstractTank {
	public static final String INL = ".inl";
	private static final String ZIP = ".zip";

	private static Logger logger = LoggerFactory.getLogger(Tank.class);
	private File currentFile = null;
	private Writer writer = null;

	public Tank(io.intino.tara.magritte.Node node) {
		super(node);
	}

	public Tank init() {
		directory().mkdirs();
		return this;
	}

	public void drop(String textMessage) {
		Message message;
		try {
			message = load(textMessage);
		} catch (Throwable e) {
			logger.error("error processing message: " + textMessage);
			logger.error(e.getMessage(), e);
			return;
		}
		drop(message);
	}

	public void drop(Message message) {
		final File inlFile = inlFile(directory(), message);
		append(inlFile, message, message.toString());
	}

	public void sort() {
		new Sorter(this).sort();
	}

	public void sort(Instant instant) {
		new Sorter(this).sort(instant);
	}

	public void seal() {
		try {
			for (File file : requireNonNull(directory().listFiles((f, n) -> n.endsWith(INL)))) {
				final byte[] text = readFile(file);
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(file.getAbsolutePath().replace(INL, ZIP))));
				ZipEntry e = new ZipEntry(file.getName());
				out.putNextEntry(e);
				out.write(text, 0, text.length);
				out.closeEntry();
				out.close();
				file.delete();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void delete$() {
		directory().renameTo(new File(graph().core$().as(DatalakeGraph.class).directory(), "old." + qualifiedName()));
		super.delete$();
	}

	private static void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
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

	public Iterator<Message> sortedMessagesIterator(Instant from) {
		return new SortedMessagesIterator().iterator(from);
	}

	private void append(File inlFile, Message message, String textMessage) {
		try {
			if (writer == null || !currentFile.equals(inlFile)) {
				if (writer != null) writer.close();
				if (!inlFile.exists()) inlFile.createNewFile();
				currentFile = inlFile;
				writer = new BufferedWriter(new FileWriter(inlFile, true));
			}
			writer.write(textMessage + "\n\n");
			saveAttachments(inlFile.getParentFile(), message);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
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

	private File inlFile(File tankDirectory, Message message) {
		return tsOf(message) == null ? null : new File(tankDirectory, fileFromInstant(tsOf(message)) + INL);
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}

	private String fileFromInstant(String instant) {
		return graph().scale().equals(Scale.Day) ? dayOf(instant) : hourOf(instant);
	}

	private List<File> filter(List<File> files, Instant from) {
		if (files.isEmpty()) return files;
		final String day = fileFromInstant(from);
		Collections.sort(files);
		final File bound = files.stream().filter(f -> f.getName().equals(day + INL) || f.getName().replace(INL, "").compareTo(day) > 0).findFirst().orElse(null);
		return bound == null || files.isEmpty() ? Collections.emptyList() : files.subList(files.indexOf(bound), files.size());
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	private static String hourOf(String instant) {
		return dayOf(instant) + instant.substring(instant.indexOf("T") + 1, instant.indexOf(":"));
	}

	public void rename(String name) {
		directory().renameTo(new File(graph().directory(), name));
	}

	public class SortedMessagesIterator {

		public Iterator<Message> iterator(Instant from) {
			try {
				File[] files = directory().listFiles((f, n) -> n.endsWith(Tank.INL) || n.endsWith(Tank.ZIP));
				if (files == null) files = new File[0];
				final List<File> fileList = filter(Arrays.asList(files), from);
				MessageInputStream stream = MessageInputStreamBuilder.of(fileList, from);
				return new Iterator<Message>() {
					Message last = stream.next();

					public boolean hasNext() {
						return last != null;
					}

					public Message next() {
						try {
							Message result = last;
							last = stream.next();
							if (last == null) {
								try {
									stream.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							return result;
						} catch (IOException e) {
							Tank.logger.error(e.getMessage(), e);
							return null;
						}
					}
				};
			} catch (Throwable e) {
				Tank.logger.error(e.getMessage(), e);
				return null;
			}
		}
	}
}