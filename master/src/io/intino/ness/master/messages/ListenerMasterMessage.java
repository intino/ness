package io.intino.ness.master.messages;

public class ListenerMasterMessage extends MasterMessage {

	private final String serverName;
	private final String clientName;
	private final Action action;
	private final String updateMessageId;
	private final String recordId;
	private final String record;

	public ListenerMasterMessage(String serverName, String clientName, Action action, String updateMessageId, String recordId, String record) {
		this.serverName = serverName;
		this.clientName = clientName;
		this.action = action;
		this.updateMessageId = updateMessageId;
		this.recordId = recordId;
		this.record = record;
	}

	public String serverName() {
		return serverName;
	}

	public String clientName() {
		return clientName;
	}

	public Action action() {
		return action;
	}

	public String updateMessageId() {
		return updateMessageId;
	}

	public String recordId() {
		return recordId;
	}

	public String record() {
		return record;
	}

	public enum Action {
		Created, Updated, Enabled, Disabled, Removed, None
	}
}
