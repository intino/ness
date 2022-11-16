package io.intino.ness.master.messages;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public class UpdateMasterMessage extends MasterMessage {

	private final String author;
	private final Action action;
	private final String value;
	private final Instant ts;

	public UpdateMasterMessage(String author, Action action, String value, Instant ts) {
		this.author = requireNonNull(author);
		this.action = requireNonNull(action);
		this.value = requireNonNull(value);
		this.ts = requireNonNull(ts);
	}

	public String author() {
		return author;
	}

	public Action action() {
		return action;
	}

	public String value() {
		return value;
	}

	public Instant ts() {
		return ts;
	}

	public enum Action {
		Publish, Enable, Disable, Remove
	}
}
