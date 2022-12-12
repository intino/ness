package io.intino.ness.master.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.intino.alexandria.Json;

import java.util.Map;

public class MasterMapSerializer {

	public static String serialize(Map<String, String> map) {
		return Json.toJson(map);
	}

	public static Map<String, String> deserialize(String str) {
		return new GsonBuilder().setPrettyPrinting().create().fromJson(str, new TypeToken<Map<String, String>>() {
		}.getType());
	}
}
