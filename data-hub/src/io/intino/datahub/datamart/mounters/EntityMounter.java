package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;

public final class EntityMounter extends MasterDatamartMounter {

	public EntityMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Event event) {
		if(event instanceof MessageEvent e) mount(e.toMessage());
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		try {
			String id = message.get("id").asString();
			if (isInvalidId(id) || isDisabled(message)) return;

			Message oldMessage = datamart.entityStore().get(id);

			if (oldMessage != null)
				update(message, id, oldMessage);
			else
				addNewEntity(message, id);

		} catch (Throwable e) {
			Logger.error("Failed to mount message of type " + message.type() + ": " + e.getMessage(), e);
		}
	}

	private void addNewEntity(Message message, String id) {
		datamart.entityStore().put(id, message);
	}

	private void update(Message message, String id, Message oldMessage) {
		if (!oldMessage.type().equals(message.type()))
			throw new MismatchMessageTypeException("Id " + id + " already exists with a different message type: old=" + oldMessage.type() + ", new=" + message.type());
		update(oldMessage, message);
	}

	private void update(Message message, Message changes) {
		for (String attribute : changes.attributes()) {
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
