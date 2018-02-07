package io.intino.ness.datalake;

import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

import static io.intino.ness.Inl.load;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.time.Instant.parse;


public class DatalakeManager {
	private static final String INL = ".inl";
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);

	public File getStationDirectory() {
		return stationDirectory;
	}

	private File stationDirectory;
	private Map<File, Instant> lastMessageTime = new HashMap<>();

	public DatalakeManager(String stationFolder) {
		this.stationDirectory = new File(stationFolder);
		this.stationDirectory.mkdirs();
	}

	public void addTank(Tank tank) {
		directoryOf(tank).mkdirs();
	}

	public void removeTank(Tank tank) {
		String qualifiedName = tank.qualifiedName();
		new File(stationDirectory, qualifiedName).renameTo(new File(stationDirectory, "old." + qualifiedName));
	}

	public boolean rename(Tank tank, String name) {
		return directoryOf(tank).renameTo(new File(stationDirectory, name));
	}

	public void drop(Tank tank, Message message) {
		append(inl(directoryOf(tank), message), message);
	}

	public void pump(Tank from, Tank to, Function function) {
		//TODO
	}

	public Iterator<Message> sortedMessagesIterator(Tank tank) {
		try {
			File[] files = directoryOf(tank).listFiles();
			if (files == null) files = new File[0];
			io.intino.ness.inl.MessageInputStream stream = MessageInputStream.of(files);
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

	private void append(File inlFile, Message message) {
		try {
			if (lastMessageTime.containsKey(inlFile) && shouldBeAtTheEnd(inlFile, message)) {
				write(inlFile.toPath(), (message.toString() + "\n\n").getBytes(), APPEND);
				lastMessageTime.put(inlFile, parse(message.ts()));
			} else {
				List<Message> messages = loadMessages(inlFile);
				addMessage(messages, message);
				save(inlFile, messages);
			}
			saveAttachments(inlFile.getParentFile(), message);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private boolean shouldBeAtTheEnd(File inlFile, Message message) {
		return parse(message.ts()).isAfter(lastMessageTime.get(inlFile));
	}

	private void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
	}

	private List<Message> loadMessages(File inlFile) throws IOException {
		return !inlFile.exists() ? new ArrayList<>() : load(new String(Files.readAllBytes(inlFile.toPath()), Charset.forName("UTF-8")));
	}

	private void save(File inlFile, List<Message> messages) {
		if (!inlFile.getParentFile().exists()) inlFile.getParentFile().mkdirs();
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		try {
			write(inlFile.toPath(), builder.toString().getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private File inl(File tankDirectory, Message message) {
		return message.ts() == null ? null : new File(tankDirectory, dayOf(message.ts()) + INL);
	}

	private static String dayOf(String instant) {
		return instant.replace("-", "").substring(0, 8);
	}

	private File directoryOf(Tank tank) {
		return new File(stationDirectory, tank.qualifiedName());
	}

	private void addMessage(List<Message> list, Message element) {
		if (list.size() == 0) list.add(element);
		else if (parse(list.get(0).ts()).compareTo(parse(element.ts())) > 0) list.add(0, element);
		else if (parse(list.get(list.size() - 1).ts()).compareTo(parse(element.ts())) < 0) list.add(list.size(), element);
		else {
			int i = 0;
			while (parse(list.get(i).ts()).compareTo(parse(element.ts())) < 0) i++;
			list.add(i, element);
		}
	}

}
