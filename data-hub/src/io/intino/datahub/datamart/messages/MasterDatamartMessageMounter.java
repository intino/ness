package io.intino.datahub.datamart.messages;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;

import java.util.List;

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
			if(oldMessage != null)
				update(oldMessage, message);
			else
				datamart.put(id, message);
		} catch (Throwable e) {
			Logger.error("Failed to mount message of type " + message.type() + ": " + e.getMessage(), e);
		}
	}

	private void update(Message message, Message changes) {
		for(String attribute : changes.attributes()) {
			message.set(attribute, changes.get(attribute).data());
		}
		// TODO: how to update old components??
		List<Message> components = message.components();
		for(Message newComponent : changes.components()) {
			if(!components.contains(newComponent)) message.add(newComponent);
		}
	}

	private boolean isInvalidId(String id) {
		return id == null || id.isEmpty();
	}

	private static boolean isDisabled(Message message) {
		return message.contains("enabled") && !message.get("enabled").asBoolean();
	}
}
