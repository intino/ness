package io.intino.ness.master.data.validation;

/**
 * Indicates the source of a triple. It can refer to a file or a publisher (from other application).
 */
public interface TripletSource {

	static TripletSource ofFile(String path, int line) {
		return new FileTripletSource(path, line);
	}

	static TripletSource ofPublisher(String senderName) {
		return new PublisherTripletSource(senderName);
	}

	String name();

	String get();

	class FileTripletSource implements TripletSource {

		private final String path;
		private final int line;

		public FileTripletSource(String path, int line) {
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

	class PublisherTripletSource implements TripletSource {

		private final String senderName;

		public PublisherTripletSource(String senderName) {
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
