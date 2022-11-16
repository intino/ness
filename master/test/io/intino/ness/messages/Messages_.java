package io.intino.ness.messages;

import io.intino.alexandria.Json;
import io.intino.ness.master.messages.*;

import java.time.Instant;

public class Messages_ {

	public static void main(String[] args) {

		ErrorMasterMessage m = new ErrorMasterMessage(new MasterMessageException("AAA")
				.originalMessage(new UpdateMasterMessage("abc", UpdateMasterMessage.Action.Enable, "r", Instant.now())),
				Instant.now());

		System.out.println(Json.toJsonPretty(m));

		System.out.println(MasterMessageSerializer.deserialize(MasterMessageSerializer.serialize(m), ErrorMasterMessage.class));
	}
}
