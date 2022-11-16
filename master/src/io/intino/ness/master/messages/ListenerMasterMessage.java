package io.intino.ness.master.messages;

import java.time.Instant;

public class ListenerMasterMessage extends MasterMessage {

	private final String author;
	private final Action action;
	private final String updateMessageId;
	private final String record;
	private final Instant ts;

	public ListenerMasterMessage(String author, Action action, String updateMessageId, String record, Instant ts) {
		this.author = author;
		this.action = action;
		this.updateMessageId = updateMessageId;
		this.record = record;
		this.ts = ts;
	}

	public String author() {
		return author;
	}

	public Action action() {
		return action;
	}

	public String updateMessageId() {
		return updateMessageId;
	}

	public String record() {
		return record;
	}

	@Override
	public Instant ts() {
		return ts;
	}

	public enum Action {
		Created, Updated, Enabled, Disabled, Removed
	}
}
