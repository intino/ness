package io.intino.ness.core.sessions.sorters;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;

import java.io.File;
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
		if (inMb(file.length()) > 3) new MessageFileExternalSorter(file).sort();
		else overwrite(file, new MessageTimSort<Message>().doSort(loadMessages().toArray(new Message[0]), messageComparator()));
		return file;
	}

	private void overwrite(File file, Message[] messages) {
		file.delete();
		new ZimBuilder(file).put(messages);
	}

	private List<Message> loadMessages() {
		List<Message> list = new ArrayList<>();
		ZimReader inlReader = new ZimReader(file);
		Message message;
		while ((message = inlReader.next()) != null) list.add(message);
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
