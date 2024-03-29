package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.DatamartFactory;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.model.Datamart;

import java.util.List;


public class RecreateDatamartAction {

	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String datamartName;

	public String execute() {
		try {
			return datamartName.equalsIgnoreCase("all") ? launchAllDatamartsCreation() : launchDatamartCreation();
		} catch (Throwable e) {
			Logger.error(e);
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
	}

	private String launchAllDatamartsCreation() {
		executeAsync(this::recreateAll);
		return "Recreation of all datamarts launched (" + box.graph().datamartList().size() + "). Check log for more info.";
	}

	private String launchDatamartCreation() {
		Datamart datamart = box.graph().datamartList(d -> d.name$().equals(datamartName)).findFirst().orElse(null);
		if (datamart == null) return "Datamart " + datamartName + " not found";
		executeAsync(() -> {
			new SealAction(box).execute();
			recreate(datamart);
			new Thread(() -> box.nessService().notifyDatamartReload(datamartName)).start();
		});
		return "Recreation of datamart " + datamartName + " launched. Check log for more info.";
	}

	private void recreateAll() {
		new SealAction(box).execute();
		List<Datamart> datamartList = box.graph().datamartList();
		for (int i = 0; i < datamartList.size(); i++) {
			Datamart datamart = datamartList.get(i);
			Logger.info("Creating " + datamart.name$() + " (" + (i + 1) + "/" + datamartList.size() + ")...");
			recreate(datamart);

			box.nessService().notifyDatamartReload(datamart.name$());
		}
	}

	private void recreate(Datamart definition) {
		try {
			synchronized (RecreateDatamartAction.class) {
				box.brokerService().manager().pauseTankConsumers();
				MasterDatamart datamart = box.datamarts().get(datamartName);
				if (datamart == null) {
					datamart = new LocalMasterDatamart(box, definition);
					box.datamarts().put(definition.name$(), datamart);
				}
				datamart.clear();
				new DatamartFactory(box, box.datalake()).reflow(datamart, definition);

				Logger.info("Datamart " + definition.name$() + " recreated!");
			}
		} catch (Throwable e) {
			Logger.error(e);
		} finally {
			box.brokerService().manager().startTankConsumers();
		}
	}

	private void executeAsync(Runnable action) {
		new Thread(action, getClass().getSimpleName()).start();
	}
}