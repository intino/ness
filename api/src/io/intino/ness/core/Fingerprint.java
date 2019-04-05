package io.intino.ness.core;


import io.intino.alexandria.Timetag;

public class Fingerprint {

	private static final String SEPARATOR = "/";
	private String fingerprint;

	public Fingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public static Fingerprint of(String tank, Timetag timetag) {
		return new Fingerprint(tank + SEPARATOR + timetag);
	}

	public static Fingerprint of(String tank, Timetag timetag, String set) {
		return new Fingerprint(tank + SEPARATOR + timetag + SEPARATOR + set);
	}

	public String tank() {
		return fingerprint.split(SEPARATOR)[0];
	}

	public Timetag timetag() {
		return new Timetag(fingerprint.split(SEPARATOR)[1]);
	}

	public String set() {
		try {
			return fingerprint.split(SEPARATOR)[2];
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public int size() {
		return fingerprint.length();
	}

	@Override
	public String toString() {
		return fingerprint;
	}

	@Override
	public boolean equals(Object o) {
		return fingerprint.equals(o.toString());
	}

	@Override
	public int hashCode() {
		return fingerprint.hashCode();
	}

	public String name() {
		return fingerprint.replace("/", "-");
	}
}
