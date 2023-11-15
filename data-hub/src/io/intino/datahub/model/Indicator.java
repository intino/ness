package io.intino.datahub.model;

import java.time.Instant;
import java.util.Map;

public record Indicator(Map<String, Shot> shots) {
	public Shot get(String sensor) {
		return shots.get(sensor);
	}

	public void put(String sensor, Instant instant, double value) {
		shots.put(sensor, new Shot(instant, value));
	}

	public record Shot(Instant ts, double value) {
	}
}
