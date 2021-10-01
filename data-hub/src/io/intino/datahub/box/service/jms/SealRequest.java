package io.intino.datahub.box.service.jms;

import io.intino.alexandria.event.EventHub.RequestConsumer;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.actions.SealAction;

public class SealRequest implements RequestConsumer {
	private final DataHubBox box;

	public SealRequest(DataHubBox box) {
		this.box = box;
	}

	public String accept(String request) {
		try {
			Message next = new MessageReader(request).next();
			String stage = next.get("stage").data();
			String result;
			if (stage != null) result = new SealAction(box).execute(stage);
			else result = new SealAction(box).execute();
			return result.replace(SealAction.MESSAGE_PREFIX, "");
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}
}