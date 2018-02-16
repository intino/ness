package io.intino.ness.datalake;

import io.intino.konos.alexandria.Inl;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import org.apache.activemq.util.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;

import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.Instant.parse;
import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;


public class DatalakeManager {
	private static final String INL = ".inl";
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private Map<File, List<Message>> messages = new LRUCache<>(32);

	public File getStationDirectory() {
		return stationDirectory;
	}

	private File stationDirectory;
	private Map<File, Instant> lastMessageTime = new HashMap<>();

	public DatalakeManager(String stationFolder, List<Tank> tanks) {
		this.stationDirectory = new File(stationFolder);
		this.stationDirectory.mkdirs();
		tanks.forEach(this::addTank);
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

	public void pump(Tank from, Tank to, Function function) {
		//TODO
	}

	public Iterator<Message> sortedMessagesIterator(Tank tank) {
		try {
			File[] files = directoryOf(tank).listFiles((f, n) -> n.endsWith(INL));
			if (files == null) files = new File[0];
			final List<File> fileList = Arrays.asList(files);
			Collections.sort(fileList);
			io.intino.ness.inl.MessageInputStream stream = MessageInputStreamBuilder.of(fileList);
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

	private void append(File inlFile, Message message, String textMessage) {
		final Instant messageInstant = parse(tsOf(message));
		try {
			final boolean existFile = lastMessageTime.containsKey(inlFile);
			if (!existFile) writeAndUpdate(inlFile, message, textMessage, messageInstant, CREATE);
			else if (shouldBeAtTheEnd(inlFile, messageInstant)) writeAndUpdate(inlFile, message, textMessage, messageInstant, APPEND);
			else appendMessage(inlFile, message);
			saveAttachments(inlFile.getParentFile(), message);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void writeAndUpdate(File inlFile, Message message, String textMessage, Instant messageInstant, StandardOpenOption create) throws IOException {
		write(inlFile.toPath(), (textMessage + "\n\n").getBytes(), create);
		updateCache(inlFile, message, messageInstant);
	}

	private void updateCache(File inlFile, Message message, Instant messageInstant) {
		this.messages.putIfAbsent(inlFile, new ArrayList<>());
		if (this.messages.containsKey(inlFile)) this.messages.get(inlFile).add(message);
		lastMessageTime.put(inlFile, messageInstant);
	}

	private void appendMessage(File inlFile, Message message) throws IOException {
		List<Message> messages = getMessages(inlFile);
		addMessage(messages, message);
		save(inlFile, messages);
	}

	private List<Message> getMessages(File inlFile) throws IOException {
		List<Message> messages;
		if (this.messages.containsKey(inlFile)) messages = this.messages.get(inlFile);
		else {
			messages = loadMessages(inlFile);
			this.messages.put(inlFile, messages);
		}
		return messages;
	}

	private boolean shouldBeAtTheEnd(File inlFile, Instant messageInstant) {
		final Instant lastInstant = lastMessageTime.get(inlFile);
		return lastInstant == null || messageInstant.isAfter(lastInstant);
	}

	private void saveAttachments(File directory, Message message) {
//		for (Attachment attachment : message.attachments()) {
//			Files.write(new File(directory, attachment.name()).toPath(), attachment.asByteArray());
//		}
	}

	private List<Message> loadMessages(File inlFile) throws IOException {
		return Inl.load(new String(Files.readAllBytes(inlFile.toPath()), Charset.forName("UTF-8")));
	}

	private void save(File inlFile, List<Message> messages) {
		StringBuilder builder = new StringBuilder();
		for (Message m : messages) builder.append(m.toString()).append("\n\n");
		try {
			write(inlFile.toPath(), builder.toString().getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
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

	private void addMessage(List<Message> list, Message element) {
		if (list.size() == 0) list.add(element);
		else {
			final int position = findPosition(list, element);
			list.add(position < 0 ? Math.abs(position) - 1 : position, element);
		}
	}

	private int findPosition(List<Message> list, Message element) {
		return binarySearch(list, element, comparing(m -> parse(tsOf(m))));
	}
}