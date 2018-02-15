package io.intino.ness.datalake;

import io.intino.ness.inl.Loader;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MessageInputStreamBuilder {

	private static Logger logger = LoggerFactory.getLogger(MessageInputStreamBuilder.class);

	public static io.intino.ness.inl.MessageInputStream of(List<File> files) {
		return new SortedCollectionMessageStream(files.stream().map(MessageInputStreamBuilder::streamOf).collect(Collectors.toList()));
	}

	private static MessageInputStream streamOf(File file) {
		try {
			final MessageInputStream stream = Loader.Inl.of(new FileInputStream(file));
			stream.name(file.getName());
			return stream;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	static class SortedCollectionMessageStream implements io.intino.ness.inl.MessageInputStream {

		private final List<MessageInputStream> streams;
		private MessageInputStream current;
		private int currentIndex = 0;


		SortedCollectionMessageStream(List<io.intino.ness.inl.MessageInputStream> streams) {
			super();
			this.streams = streams;
			this.current = streams.get(0);
		}

		public Message next() throws IOException {
			if (current == null) return null;
			final Message message = current.next();
			if (message == null) {
				nextStream();
				return current == null ? null : current.next();
			} else return message;
		}

		public boolean hasNext() {
			return current.hasNext() || currentIndex < streams.size();
		}

		public void close() throws IOException {
			if (current != null) current.close();
		}

		private MessageInputStream nextStream() {
			try {
				close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return current = currentIndex < streams.size() ? streams.get(currentIndex = ++currentIndex) : null;
		}

		public String name() {
			return "collection stream";
		}

		public void name(String value) {
		}
	}
}
