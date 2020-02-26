package io.intino.datahub.service.jms;

import io.intino.alexandria.event.EventHub.RequestConsumer;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.datahub.DataHub;
import io.intino.datahub.datalake.actions.SealingAction;

public class SealRequest implements RequestConsumer {

	private DataHub dataHub;

	public SealRequest(DataHub dataHub) {
		this.dataHub = dataHub;
	}

	public String accept(String request) {
		try {
			Message next = new MessageReader(request).next();
			next.get("stage");
			new SealingAction(dataHub).execute();
			return String.valueOf(true);
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}

}