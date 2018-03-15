package io.intino.ness.datalake;

import io.intino.konos.alexandria.Inl;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;


public class DatalakeManager {
	private static final String INL = ".inl";
	private static final String ZIP = ".zip";
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private File stationDirectory;

	public DatalakeManager(String stationFolder, List<Tank> tanks) {
		this.stationDirectory = new File(stationFolder);
		this.stationDirectory.mkdirs();
		tanks.forEach(this::addTank);
	}

	public File getStationDirectory() {
		return stationDirectory;
	}

	public void addTank(Tank tank) {
		directoryOf(tank).mkdirs();
	}

	public void removeTank(Tank tank) {
		new File(stationDirectory, tank.qualifiedName()).renameTo(new File(stationDirectory, "old." + tank.qualifiedName()));
	}

	public boolean rename(Tank tank, String name) {
		return directoryOf(tank).renameTo(new File(stationDirectory, name));
	}

	public void drop(Tank tank, Message message, String textMessage) {
		append(inl(directoryOf(tank), message), message, textMessage);
	}

	public void sort(Tank tank) {
		try {
			for (File file : Objects.requireNonNull(directoryOf(tank).listFiles((f, n) -> f.getName().endsWith(INL)))) sort(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sort(Tank tank, Instant day) {
		try {
			for (File file : Objects.requireNonNull(directoryOf(tank).listFiles((f, n) -> f.getName().endsWith(INL) && f.getName().equals(dayOf(day.toString()) + INL))))
				sort(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sort(File file) throws IOException {
		final List<Message> messages = loadMessages(file);
		messages.sort(Comparator.comparing(m -> Instant.parse(tsOf(m))));
		save(file, messages);
	}

	public void seal(Tank tank) {
		try {
			for (File file : Objects.requireNonNull(directoryOf(tank).listFiles((f, n) -> f.getName().endsWith(INL)))) {
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

	public void pump(Tank from, Tank to, Function function) {
		//TODO
	}

	public Iterator<Message> sortedMessagesIterator(Tank tank, Instant from) {
		try {
			File[] files = directoryOf(tank).listFiles((f, n) -> n.endsWith(INL));
			if (files == null) files = new File[0];
			final List<File> fileList = filter(Arrays.asList(files), from);
			io.intino.ness.inl.MessageInputStream stream = MessageInputStreamBuilder.of(fileList, from);
			return new Iterator<Message>() {
				public boolean hasNext() {
					return stream.hasNext();
				}

				public Message next() {
					try {
						return stream.next();
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

	private List<File> filter(List<File> files, Instant from) {
		if (files.isEmpty()) return files;
		final String day = dayOf(from.toString());
		Collections.sort(files);
		final File bound = files.stream().filter(f -> f.getName().equals(day + INL) || f.getName().compareTo(day) > 0).findFirst().orElse(null);
		return files.subList(files.indexOf(bound), files.size());
	}

	private void append(File inlFile, Message message, String textMessage) {
		write(inlFile, textMessage, inlFile.exists() ? APPEND : CREATE);
		saveAttachments(inlFile.getParentFile(), message);
	}

	private void save(File inlFile, List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		write(inlFile, builder.toString(), CREATE);
	}

	private synchronized void write(File inlFile, String textMessage, StandardOpenOption option) {
		try {
			Files.write(inlFile.toPath(), (textMessage + "\n\n").getBytes(), option);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
	}

	private List<Message> loadMessages(File inlFile) throws IOException {
		return Inl.load(new String(readFile(inlFile), Charset.forName("UTF-8")));
	}

	private byte[] readFile(File inlFile) throws IOException {
		return Files.readAllBytes(inlFile.toPath());
	}

	private File inl(File tankDirectory, Message message) {
		return tsOf(message) == null ? null : new File(tankDirectory, dayOf(tsOf(message)) + INL);
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	private File directoryOf(Tank tank) {
		return new File(stationDirectory, tank.qualifiedName());
	}
}