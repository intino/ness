package io.intino.master.model;

import java.io.Serializable;
import java.util.Objects;

import static io.intino.master.core.Master.NONE_TYPE;

public class Triple implements Serializable {

	public static final String SEPARATOR = "\t";

	public static String typeOf(Triple triple) {
		return triple.type();
	}

	public static String typeOf(String subject) {
		final int start = subject.indexOf(':');
		return start < 0 ? NONE_TYPE : subject.substring(start + 1);
	}

	private final String subject, predicate, value;

	public Triple(String line) {
		this(lineToTriple(line));
	}

	public Triple(String[] split) {
		this(split[0], split[1], split[2]);
	}

	public Triple(String subject, String predicate, String value) {
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
	}

	public String type() {
		return typeOf(subject);
	}

	public String subject() {
		return subject;
	}

	public String predicate() {
		return predicate;
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Triple triple = (Triple) o;
		return Objects.equals(subject, triple.subject) && Objects.equals(predicate, triple.predicate) && Objects.equals(value, triple.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject, predicate, value);
	}

	@Override
	public String toString() {
		return subject + SEPARATOR + predicate + SEPARATOR + value;
	}

	private static String[] lineToTriple(String line) {
		String[] triple = line.split(SEPARATOR);
		if(triple.length != 3) throw new IllegalArgumentException("Line triple (" + line + ") is malformed: it must have 3 fields separated by TAB, but it has " + triple.length);
		return triple;
	}
}
