package io.intino.ness.core;

import io.intino.alexandria.zet.ZetStream;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ZetReader implements ZetStream {


	private final Iterator<Long> iterator;
	private long current = -1;

	public ZetReader(File file) {
		this(iteratorOf(inputStream(file)));
	}

	public ZetReader(InputStream is) {
		this(iteratorOf(is));
	}

	public ZetReader(long... ids) {
		this(stream(ids).boxed());
	}

	public ZetReader(List<Long> ids) {
		this(ids.stream());
	}

	public ZetReader(Stream<Long> stream) {
		this(stream.sorted().iterator());
	}

	public ZetReader(Iterator<Long> iterator) {
		this.iterator = iterator;
	}

	private static Iterator<Long> iteratorOf(InputStream is) {
		DataInputStream stream = new DataInputStream(new BufferedInputStream(is));
		return new Iterator<Long>() {
			private long current = -1;
			private long next = -1;

			@Override
			public Long next() {
				if (current == next) hasNext();
				current = next;
				return current;
			}

			@Override
			public boolean hasNext() {
				if (current != next) return true;
				try {
					next = stream.readLong();
					return true;
				} catch (IOException e) {
					next = -1;
					close();
					return false;
				}
			}

			private void close() {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private static InputStream inputStream(File file) {
		try {
			return new BufferedInputStream(new FileInputStream(file));
		} catch (IOException e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	}

	@Override
	public long current() {
		return current;
	}

	@Override
	public long next() {
		return current = iterator.hasNext() ? iterator.next() : -1L;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}
}
