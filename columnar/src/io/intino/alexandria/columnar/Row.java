package io.intino.alexandria.columnar;

import io.intino.alexandria.Timetag;

import java.util.HashMap;
import java.util.Map;

public class Row {

	private final Map<String, String> values = new HashMap<>();
	private final long id;
	private Timetag timetag;


	public Row(long id, Timetag timetag) {
		this.id = id;
		this.timetag = timetag;
	}

	public long id() {
		return id;
	}

	public Timetag timetag() {
		return timetag;
	}

	public Map<String, String> values() {
		return values;
	}

	public void put(String columnName, String value) {
		values.put(columnName, value);
	}

	public String get(String columnName) {
		return values.getOrDefault(columnName, null);
	}
}
