package io.intino.ness.master.messages;

import io.intino.alexandria.message.Message;

public class UpdateMasterMessage extends MasterMessage {

	public UpdateMasterMessage(String clientName, Intent intent, String value) {
		message.set("clientName", clientName);
		message.set("intent", intent.name());
		message.set("value", value);
	}

	public UpdateMasterMessage(Message message) {
		super(message);
	}

	public String clientName() {
		return message.get("clientName").asString();
	}

	public Intent intent() {
		return Intent.valueOf(message.get("intent").asString());
	}

	public String value() {
		return message.get("value").asString();
	}

	public enum Intent {
		Publish, Enable, Disable
	}
}
