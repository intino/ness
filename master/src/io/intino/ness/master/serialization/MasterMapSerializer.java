package io.intino.ness.master.serialization;

import io.intino.alexandria.Json;

import java.util.Map;

public class MasterMapSerializer {

	public static String serialize(Map<String, String> map) {
		return Json.toJson(map);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> deserialize(String str) {
		return Json.fromJson(str, Map.class);
	}
}
