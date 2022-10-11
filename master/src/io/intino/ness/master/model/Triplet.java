package io.intino.ness.master.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import static io.intino.ness.master.core.Master.NONE_TYPE;

/**
 * <p>A Triplet is a TAB separated list of values that defines the following fields:</p>
 * <ul>
 *     <li>Subject.</li>
 *     <li>Predicate.</li>
 *     <li>Value.</li>
 *     <li>[OPTIONAL] Author</li>
 *     <li>[OPTIONAL] Other attributes, separated by TAB.</li>
 * </ul>
 *
 * <p>Subject and Predicate must be non-blank strings.</p>
 *
 * <p>The subject is divided into 2 sections: id and type, separated by colon (:).</p>
 * <p>Example: i9-10900K:cpu => (id = i9-10900K, type = cpu)</p>
 *
 * */
public class Triplet implements Serializable {

	public static final String TRIPLET_SEPARATOR = "\t";
	public static final int TRIPLET_MIN_SIZE = 3;

	public static String typeOf(Triplet triplet) {
		return triplet.type();
	}

	public static String typeOf(String subject) {
		final int start = subject.indexOf(':');
		return start < 0 ? NONE_TYPE : subject.substring(start + 1);
	}

	private final String[] attributes;

	public Triplet(String line) {
		this(line.split(TRIPLET_SEPARATOR));
	}

	public Triplet(String[] record) {
		check(record);
		this.attributes = record;
	}

	public Triplet(String subject, String predicate, String value) {
		this.attributes = new String[] {subject, predicate, value};
	}

	public Triplet(String subject, String predicate, String value, String author) {
		this.attributes = new String[] {subject, predicate, value, author};
	}

	public String type() {
		return typeOf(subject());
	}

	public String subject() {
		return attributes[0];
	}

	public String predicate() {
		return attributes[1];
	}

	public String value() {
		return attributes[2];
	}

	public String author() {
		return attributes[3];
	}

	public String get(int index) {
		return attributes[index];
	}

	public List<String> attributes() {
		return new AbstractList<>() {
			@Override
			public String get(int index) {
				return attributes[index];
			}
			@Override
			public int size() {
				return attributes.length;
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Triplet triplet = (Triplet) o;
		return Arrays.equals(attributes, triplet.attributes);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(attributes);
	}

	@Override
	public String toString() {
		return String.join(TRIPLET_SEPARATOR, attributes);
	}

	private void check(String[] record) {
		if(record.length < TRIPLET_MIN_SIZE) throw new IllegalArgumentException("Triplets must have at least 3 fields " +
				"(subject, predicate and value), but it has " + record.length + ": " + Arrays.toString(record));
	}
}
