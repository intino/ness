package io.intino.datahub.datamart.messages;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;

public class MasterDatamartMessageMounter {

	private final MasterDatamart<Message> datamart;

	public MasterDatamartMessageMounter(MasterDatamart<Message> datamart) {
		this.datamart = datamart;
	}

	public void mount(Message message) {
		try {
			String id = message.get("id").asString();
			if (isInvalidId(id) || isDisabled(message)) return;
			Message oldMessage = datamart.get(id);
			if(oldMessage != null) {
				if(!oldMessage.type().equals(message.type())) throw new MismatchMessageTypeException("Id " + id + " already exists with a different message type: old=" + oldMessage.type() + ", new=" + message.type());
				update(oldMessage, message);
			} else {
				datamart.put(id, message);
			}
		} catch (Throwable e) {
			Logger.error("Failed to mount message of type " + message.type() + ": " + e.getMessage(), e);
		}
	}

	private void update(Message message, Message changes) {
		for(String attribute : changes.attributes()) {
			message.set(attribute, changes.get(attribute).data());
		}
		removeAllComponents(message);
		message.add(changes.components());
	}

	private void removeAllComponents(Message message) {
		message.components().forEach(message::remove);
	}

	private boolean isInvalidId(String id) {
		return id == null || id.isEmpty();
	}

	private static boolean isDisabled(Message message) {
		return message.contains("enabled") && !message.get("enabled").asBoolean();
	}

	private static class MismatchMessageTypeException extends IllegalStateException {
		public MismatchMessageTypeException(String message) {
			super(message);
		}
	}
}
