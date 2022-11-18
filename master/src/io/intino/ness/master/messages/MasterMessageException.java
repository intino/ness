package io.intino.ness.master.messages;

public class MasterMessageException extends Exception {

	private String serverName;
	private String clientName;
	private String originalMessageId;
	private Object originalMessage;

	public MasterMessageException() {
	}

	public MasterMessageException(String message) {
		super(message);
	}

	public MasterMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public String serverName() {
		return serverName;
	}

	public MasterMessageException serverName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	public String clientName() {
		return clientName;
	}

	public MasterMessageException clientName(String clientName) {
		this.clientName = clientName;
		return this;
	}

	public String originalMessageId() {
		return originalMessageId;
	}

	public Object originalMessage() {
		return originalMessage;
	}

	public MasterMessageException originalMessage(MasterMessage originalMessage) {
		this.originalMessageId = originalMessage.id();
		this.originalMessage = MasterMessageSerializer.serialize(originalMessage);
		return this;
	}
}
