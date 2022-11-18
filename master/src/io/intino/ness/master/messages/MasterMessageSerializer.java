package io.intino.ness.master.messages;

import io.intino.alexandria.Json;

public class MasterMessageSerializer {
	
	public static Object serialize(MasterMessage message) {
		return Json.toString(message);
	}

	public static <T extends MasterMessage> T deserialize(Object serializedMessage, Class<T> clazz) {
		return Json.fromString((String)serializedMessage, clazz);
	}
}
