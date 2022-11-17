package io.intino.ness.master.messages;

import java.util.UUID;

class MasterMessageIdGenerator {

	static String generate(Class<? extends MasterMessage> messageClass) {
		return messageClass.getSimpleName().replace("MasterMessage", "") + "#" + UUID.randomUUID();
	}
}
