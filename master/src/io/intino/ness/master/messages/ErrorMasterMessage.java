package io.intino.ness.master.messages;

import java.time.Instant;

public class ErrorMasterMessage extends MasterMessage {

	private final MasterMessageException error;

	public ErrorMasterMessage(MasterMessageException error) {
		this.error = error;
	}

	public MasterMessageException error() {
		return error;
	}
}
