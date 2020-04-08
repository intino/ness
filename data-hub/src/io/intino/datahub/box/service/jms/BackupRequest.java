package io.intino.datahub.box.service.jms;

import io.intino.alexandria.event.EventHub.RequestConsumer;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.actions.BackupAction;

public class BackupRequest implements RequestConsumer {

	private DataHubBox box;

	public BackupRequest(DataHubBox box) {
		this.box = box;
	}

	public String accept(String request) {
		try {
			return new BackupAction(box).execute();
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}

}