package io.intino.datahub.box.service.jms;

import io.intino.datahub.box.DataHubBox;

import javax.jms.Message;

public class EntityStoreRequest {
	public EntityStoreRequest(DataHubBox box) {
	}

	public Message accept(Message m) {
		return null;
	}
}
