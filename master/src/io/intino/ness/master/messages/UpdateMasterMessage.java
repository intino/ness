package io.intino.ness.master.messages;

public class UpdateMasterMessage extends MasterMessage {

	private final String clientName;
	private final Action action;
	private final String value;

	public UpdateMasterMessage(String clientName, Action action, String value) {
		this.clientName = clientName;
		this.action = action;
		this.value = value;
	}

	public String clientName() {
		return clientName;
	}

	public Action action() {
		return action;
	}

	public String value() {
		return value;
	}

	public enum Action {
		Publish, Enable, Disable, Remove
	}
}
