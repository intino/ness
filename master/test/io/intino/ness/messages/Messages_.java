package io.intino.ness.messages;

import io.intino.alexandria.Json;
import io.intino.ness.master.messages.ErrorMasterMessage;
import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.MasterMessageSerializer;
import io.intino.ness.master.messages.UpdateMasterMessage;

public class Messages_ {

	public static void main(String[] args) {

		ErrorMasterMessage m = new ErrorMasterMessage(new MasterMessageException("AAA")
				.clientName("the client")
				.originalMessage(new UpdateMasterMessage("abc", UpdateMasterMessage.Action.Enable, "r")));

		System.out.println(Json.toJsonPretty(m));

		System.out.println(MasterMessageSerializer.deserialize(MasterMessageSerializer.serialize(m), ErrorMasterMessage.class));
	}
}
