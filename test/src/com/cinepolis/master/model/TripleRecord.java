package com.cinepolis.master.model;

import io.intino.master.model.Triple;

import java.util.Map;
import java.util.stream.Stream;

public class TripleRecord {

	private final String id;
	private final Map<String, String> attributes;

	public TripleRecord(String id, Map<String, String> attributes) {
		this.id = id;
		this.attributes = attributes;
	}

	public String id() {
		return id;
	}

	public String type() {
		return id.contains(":") ? id.substring(id.indexOf(':') + 1) : "unknown";
	}

	public Map<String, String> attributes() {
		return attributes;
	}

	public Stream<Triple> triples() {
		return attributes.entrySet().stream().map(e -> new Triple(id, e.getKey(), e.getValue()));
	}
}
