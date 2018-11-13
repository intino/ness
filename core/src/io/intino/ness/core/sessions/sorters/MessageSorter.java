package io.intino.ness.core.sessions.sorters;

import io.intino.alexandria.inl.InlReader;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MessageSorter {
	static String SEPARATOR = "\n";
	private final File file;

	public MessageSorter(File file) {
		this.file = file;
	}

	public File sort() {
		if (inMb(file.length()) > 30) new MessageFileExternalSorter(file).sort();
		else overwrite(file, new MessageTimSort<Message>().doSort(loadMessages(file).toArray(new Message[0]), messageComparator()));
		return file;
	}

	private void overwrite(File file, Message[] messages) {
		try {
			file.delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Message message : messages) writer.write(message.toString() + SEPARATOR);
			writer.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private List<Message> loadMessages(File inlFile) {
		List<Message> list = new ArrayList<>();
		try {
			InlReader inlReader = new InlReader(new FileInputStream(inlFile));
			Message message;
			while ((message = inlReader.next()) != null) {
				list.add(message);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return list;
	}

	private long inMb(long length) {
		return length / (1024 * 1024);
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> {
			final String text = m.get("ts");
			if (text == null) Logger.error("ts is null for message: " + m.toString());
			return Instant.parse(text);
		});
	}
}
