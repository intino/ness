package io.intino.ness.master.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class TripletRecord {

	private final String id;
	private final Map<String, Triplet> triplets;

	public TripletRecord(String id, Map<String, Triplet> triplets) {
		this.id = requireNonNull(id);
		this.triplets = triplets == null ? new LinkedHashMap<>() : triplets;
	}

	public TripletRecord(Map<String, Triplet> triplets) {
		if(triplets.isEmpty()) throw new IllegalArgumentException("triplets cannot be null. Consider using TripletRecord(String id) constructor instead.");
		this.id = triplets.entrySet().stream().findFirst().get().getValue().subject();
		this.triplets = triplets;
	}

	public TripletRecord(String id) {
		this(id, new LinkedHashMap<>());
	}

	public String id() {
		return id;
	}

	public String type() {
		return Triplet.typeOf(id);
	}

	public Stream<Triplet> triplets() {
		return triplets.values().stream();
	}

	public Triplet getTriplet(String name) {
		return triplets.get(name);
	}

	public String getValue(String name) {
		Triplet t = getTriplet(name);
		return t == null ? null : t.value();
	}

	public void put(Triplet triplet) {
		triplets.put(triplet.predicate(), triplet);
	}

	public boolean contains(String predicate) {
		return triplets.containsKey(predicate);
	}

	public boolean contains(Triplet triplet) {
		return Objects.equals(triplet, getTriplet(triplet.predicate()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TripletRecord that = (TripletRecord) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return triplets().map(Triplet::toString).collect(Collectors.joining("\n"));
	}
}
