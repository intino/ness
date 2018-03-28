package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessageExternalSorter {
	private static final String INL = ".inl";

	private Logger logger = LoggerFactory.getLogger(MessageExternalSorter.class);
	private File file;
	private final File directory;


	public MessageExternalSorter(File file) {
		this.file = file;
		this.directory = this.file.getParentFile();
	}

	public void sort() {
		try {
			final MessageInputStream stream = FileMessageInputStream.of(file);
			List<File> files = processBatchs(stream);
			stream.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<File> processBatchs(MessageInputStream stream) throws IOException {
		List<File> batchs = new ArrayList<>();
		List<Message> current = new ArrayList<>();
		int i = 0;
		int batch = 0;
		while (stream.hasNext()) {
			current.add(stream.next());
			i++;
			if (i == 10000) {
				batchs.add(processBatch(current, batch = batch++));
				i = 0;
			}
		}
		if (!current.isEmpty()) batchs.add(processBatch(current, batch++));
		return batchs;
	}

	private File processBatch(List<Message> messages, int batch) {
		final File temp = new File(directory + this.file.getName().replace(INL, ""));
		messages.sort(messageComparator());
		temp.mkdirs();
		MessageSaver.save(new File(temp, batch + ""), messages);
		messages.clear();
		return temp;
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}


}
