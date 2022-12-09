package io.intino.ness.master.messages;

import io.intino.alexandria.message.Message;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public abstract class MasterMessage implements Serializable {

	public static final String INL_TYPE = "MasterMessage";

	protected final Message message;

	public MasterMessage() {
		this.message = new Message(INL_TYPE);
		setBaseInfo();
	}

	public MasterMessage(Message message) {
		this.message = message;
	}

	public String messageClass() {
		return message.get("messageClass").asString();
	}

	public final String id() {
		return message.get("id").asString();
	}

	public Instant ts() {
		return message.get("ts").asInstant();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MasterMessage that = (MasterMessage) o;
		return Objects.equals(id(), that.id());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id());
	}

	@Override
	public String toString() {
		return message.toString();
	}

	private void setBaseInfo() {
		message.set("messageClass", getClass().getName());
		message.set("id", MasterMessageIdGenerator.generateFor(getClass()));
		message.set("ts", Instant.now());
	}

	public static class Unknown extends MasterMessage {
		public Unknown(Message message) {
			super(message);
		}
	}
}
