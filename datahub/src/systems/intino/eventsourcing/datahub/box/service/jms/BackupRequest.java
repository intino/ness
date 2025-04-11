package systems.intino.eventsourcing.datahub.box.service.jms;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.box.actions.BackupAction;
import systems.intino.eventsourcing.event.EventHub.RequestConsumer;

public class BackupRequest implements RequestConsumer {

	private DatahubBox box;

	public BackupRequest(DatahubBox box) {
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