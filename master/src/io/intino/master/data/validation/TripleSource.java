package io.intino.master.data.validation;

/**
 * Indicates the source of a triple. It can refer to a file or a publisher (from other application).
 * */
public interface TripleSource {

	static TripleSource ofFile(String path, int line) {
		return new FileTripleSource(path, line);
	}

	static TripleSource ofPublisher(String senderName) {
		return new PublisherTripleSource(senderName);
	}

	String name();

	String get();

	class FileTripleSource implements TripleSource {

		private final String path;
		private final int line;

		public FileTripleSource(String path, int line) {
			this.path = path;
			this.line = line;
		}

		@Override
		public String name() {
			return path;
		}

		@Override
		public String get() {
			return "File " + path + "(" + line + ")";
		}

		public String path() {
			return path;
		}

		public int line() {
			return line;
		}

		@Override
		public String toString() {
			return get();
		}
	}

	class PublisherTripleSource implements TripleSource {

		private final String senderName;

		public PublisherTripleSource(String senderName) {
			this.senderName = senderName;
		}

		@Override
		public String name() {
			return senderName;
		}

		@Override
		public String get() {
			return "Publisher '" + senderName + "'";
		}

		@Override
		public String toString() {
			return get();
		}
	}
}
