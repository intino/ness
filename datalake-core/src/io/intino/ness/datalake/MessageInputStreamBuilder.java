package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static io.intino.ness.datalake.AttachmentLoader.loadAttachments;

public class MessageInputStreamBuilder {

	private static Logger logger = LoggerFactory.getLogger(MessageInputStreamBuilder.class);

	public static MessageInputStream of(List<File> files, Instant from) {
		return new SortedCollectionMessageStream(files, from);
	}

	static class SortedCollectionMessageStream implements MessageInputStream {
		private final List<File> files;
		private MessageInputStream current;
		private Message lastMessage;
		private int currentIndex = 0;

		SortedCollectionMessageStream(List<File> files, Instant from) {
			this.files = files;
			this.current = files.isEmpty() ? null : streamOf(this.files.get(0));
			advanceTo(from);
		}

		public Message next() throws IOException {
			if (current == null) return null;
			Message last = lastMessage;
			lastMessage = loadNext();
			return last;
		}

		private void advanceTo(Instant from) {
			if (current == null) return;
			try {
				while (true) {
					this.lastMessage = loadNext();
					if (lastMessage == null) return;
					final Instant ts = Instant.parse(lastMessage.get("ts"));
					if (ts.equals(from) || ts.isAfter(from)) return;
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		private Message loadNext() throws IOException {
			final Message message = current.next();
			if (message == null) {
				nextStream();
				final Message next = current == null ? null : current.next();
				if (next == null) return null;
				loadAttachments(new File(current.name()), next);
				return next;
			} else return message;
		}

		public boolean hasNext() {
			return current.hasNext() || currentIndex < files.size();
		}

		public void close() throws IOException {
			if (current != null) current.close();
		}

		private void nextStream() {
			try {
				close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			current = currentIndex < files.size() - 1 ? streamOf(files.get(currentIndex = ++currentIndex)) : null;
		}

		private static MessageInputStream streamOf(File file) {
			try {
				final MessageInputStream stream = FileMessageInputStream.of(file);
				stream.name(file.getPath());
				return stream;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}

		public String name() {
			return "collection stream";
		}

		public void name(String value) {
		}

	}
}
