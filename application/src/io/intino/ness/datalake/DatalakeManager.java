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
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;


public class DatalakeManager {
	private static final String INL = ".inl";
	private static final String ZIP = ".zip";
	private static Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private final Scale scale;
	private File stationDirectory;

	public DatalakeManager(String stationFolder, Scale scale, List<Tank> tanks) {
		this.stationDirectory = new File(stationFolder);
		this.scale = scale;
		this.stationDirectory.mkdirs();
		tanks.forEach(this::addTank);
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	private static String hourOf(String instant) {
		return dayOf(instant) + instant.substring(instant.indexOf("T") + 1, instant.indexOf(":"));
	}

	public static String toString(List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		return builder.toString();
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
		final File inlFile = inlFile(directoryOf(tank), message);
		tank.append(inlFile, message, textMessage);
		if (tank.sorted().contains(inlFile.getName())) {
			tank.sorted().remove(inlFile.getName());
			tank.save$();
		}
	}

	public void sort(Tank tank) {
		try {
			tank.flush();
			for (File file : requireNonNull(directoryOf(tank).listFiles((f, n) -> n.endsWith(INL) && !tank.sorted().contains(n) && !isCurrentFile(n)))) {
				sort(tank, file);
				markAsSorted(tank, file);
			}
			logger.info("sorted " + tank.qualifiedName());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sort(Tank tank, Instant instant) {
		try {
			final List<File> inlFiles = Arrays.asList(Objects.requireNonNull(directoryOf(tank).listFiles((f, n) -> n.endsWith(INL) && !isCurrentFile(n))));
			for (File file : instant == null ? inlFiles : inlFiles.stream().filter(f -> f.getName().equals(fileFromInstant(instant) + INL)).collect(Collectors.toList())) {
				sort(tank, file);
				markAsSorted(tank, file);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sort(Tank tank, File file) throws IOException {
		logger.info("sorting " + tank.qualifiedName() + " " + file.getName());
		if (inMb(file.length()) > 50) new MessageExternalSorter(file).sort();
		else {
			final List<Message> messages = loadMessages(file);
			messages.sort(messageComparator());
			tank.overwrite(file, messages);
		}
	}

	private long inMb(long length) {
		return length / (1024 * 1024);
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> {
			final String text = tsOf(m);
			if (text == null) logger.error("ts is null for message: " + m.toString());
			return Instant.parse(text);
		});
	}

	private void markAsSorted(Tank tank, File file) {
		tank.sorted().add(file.getName());
		tank.save$();
	}

	public void seal(Tank tank) {
		try {
			for (File file : requireNonNull(directoryOf(tank).listFiles((f, n) -> n.endsWith(INL)))) {
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
		final String day = fileFromInstant(from);
		Collections.sort(files);
		final File bound = files.stream().filter(f -> f.getName().equals(day + INL) || f.getName().replace(INL, "").compareTo(day) > 0).findFirst().orElse(null);
		return bound == null || files.isEmpty() ? Collections.emptyList() : files.subList(files.indexOf(bound), files.size());
	}

	private List<Message> loadMessages(File inlFile) throws IOException {
		return Inl.load(new String(readFile(inlFile), Charset.forName("UTF-8")));
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

	private boolean isCurrentFile(String file) {
		return (fileFromInstant(Instant.now()) + INL).equals(file);
	}

	private String fileFromInstant(Instant instant) {
		return fileFromInstant(instant.toString());
	}

	private String fileFromInstant(String instant) {
		return scale.equals(Scale.Day) ? dayOf(instant) : hourOf(instant);
	}

	private File directoryOf(Tank tank) {
		return new File(stationDirectory, tank.qualifiedName());
	}
}