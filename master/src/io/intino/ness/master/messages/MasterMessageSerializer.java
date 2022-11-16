package io.intino.ness.master.messages;

import io.intino.alexandria.Json;

public class MasterMessageSerializer {
	
	public static String serialize(MasterMessage message) {
		return Json.toString(message);
	}

	public static <T extends MasterMessage> T deserialize(String serializedMessage, Class<T> clazz) {
		return Json.fromString(serializedMessage, clazz);
	}
}
