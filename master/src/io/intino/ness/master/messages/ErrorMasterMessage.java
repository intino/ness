package io.intino.ness.master.messages;

import java.time.Instant;

public class ErrorMasterMessage extends MasterMessage {

	private final MasterMessageException error;
	private final Instant ts;

	public ErrorMasterMessage(MasterMessageException error, Instant ts) {
		this.error = error;
		this.ts = ts;
	}

	public MasterMessageException error() {
		return error;
	}

	public Instant ts() {
		return ts;
	}
}
