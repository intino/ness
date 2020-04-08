package io.intino.datahub.box.actions;

import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datalake.actions.DatalakeBackupAction;


public class BackupAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public BackupAction(DataHubBox box) {
		this.box = box;
	}

	public BackupAction() {
	}

	public String execute() {
		if (box.graph().datalake() == null || box.graph().datalake().backup() == null)
			return "Datalake is not configured with backups";
		DatalakeBackupAction action = new DatalakeBackupAction(box);
		if (action.isStarted()) return "Datalake backup is already started";
		else new Thread(action::execute).start();
		return "Backup Started";
	}
}