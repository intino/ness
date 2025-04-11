package systems.intino.eventsourcing.datahub.box.actions;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datalake.actions.DatalakeBackupAction;


public class BackupAction {
	public systems.intino.eventsourcing.datahub.box.DatahubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public BackupAction(DatahubBox box) {
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