package io.intino.datahub.datalake.actions;


import io.intino.datahub.DataHub;

public class BackupAction {
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public DataHub dataHub;

	public BackupAction(DataHub dataHub) {
		this.dataHub = dataHub;
	}

	public String execute() {
		DatalakeBackupAction action = new DatalakeBackupAction(dataHub);
		if (action.isStarted()) return "Datalake backup is already started";
		else new Thread(action::execute).start();
		return "Backup Started";
	}
}