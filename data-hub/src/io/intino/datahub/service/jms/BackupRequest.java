package io.intino.datahub.service.jms;

import io.intino.alexandria.event.EventHub.RequestConsumer;
import io.intino.datahub.DataHub;
import io.intino.datahub.datalake.actions.BackupAction;

public class BackupRequest implements RequestConsumer {

	private DataHub dataHub;

	public BackupRequest(DataHub dataHub) {
		this.dataHub = dataHub;
	}

	public String accept(String request) {
		try {
			new BackupAction(dataHub).execute();
			return String.valueOf(true);
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}

}