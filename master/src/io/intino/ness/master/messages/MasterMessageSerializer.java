package io.intino.ness.master.messages;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;

public class MasterMessageSerializer {

	public static String serialize(MasterMessage message) {
		return message.toString();
	}

	public static MasterMessage deserialize(String str) {
		Message message = asMessage(str);
		if(message == null) return null;
		String messageClass = message.get("messageClass").asString();
		if (messageClass == null) return new MasterMessage.Unknown(message);
		return instantiate(messageClass, message);
	}

	private static Message asMessage(String str) {
		MessageReader reader = new MessageReader(str);
		return reader.hasNext() ? reader.next() : null;
	}

	@SuppressWarnings("unchecked")
	private static MasterMessage instantiate(String messageClass, Message message) {
		try {
			Class<? extends MasterMessage> clazz = (Class<? extends MasterMessage>) Class.forName(messageClass);
			return clazz.getConstructor(Message.class).newInstance(message);
		} catch (Exception e) {
			Logger.error(e);
			return new MasterMessage.Unknown(message);
		}
	}
}
