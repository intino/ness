package io.intino.ness.inl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface MessageInputStream {

	String name();

	void name(String value);

	Message next() throws IOException;

	boolean hasNext();

	void close() throws IOException;

	class Collection implements MessageInputStream {
		private final List<Message> messages;
		private String name;
		private Iterator<Message> iterator;

		public static Collection of(MessageInputStream... inputs) throws IOException {
			List<Message> messages = new ArrayList<>();
			for (MessageInputStream input : inputs)
				messages.addAll(MessageReader.readAll(input));
			return new Collection(messages);
		}

		private Collection(List<Message> messages) {
			this.messages = messages;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public void name(String value) {
			this.name = value;
		}

		@Override
		public Message next() {
			if (iterator == null) iterator = messages.iterator();
			return iterator.hasNext() ? iterator.next() : null;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public void close() {

		}

		@Override
		public String toString() {
			return name;
		}
	}

	class Empty implements MessageInputStream {

		@Override
		public String name() {
			return "empty";
		}

		@Override
		public void name(String value) {

		}

		@Override
		public Message next() throws IOException {
			return null;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public void close() throws IOException {

		}

		@Override
		public String toString() {
			return name();
		}

	}
}
