package io.intino.ness.master.messages;

public class MasterMessageException extends Exception {

	private String originalMessage;

	public MasterMessageException() {
	}

	public MasterMessageException(String message) {
		super(message);
	}

	public MasterMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public String originalMessage() {
		return originalMessage;
	}

	public MasterMessageException originalMessage(MasterMessage originalMessage) {
		this.originalMessage = MasterMessageSerializer.serialize(originalMessage);
		return this;
	}
}
