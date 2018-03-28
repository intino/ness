package io.intino.ness.box.scheduling;

import org.quartz.*;
import io.intino.ness.box.NessBox;
import io.intino.konos.scheduling.ScheduledTrigger;

public class SortTanksTask implements ScheduledTrigger {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		NessBox box = (NessBox) context.getMergedJobDataMap().get("box");
		io.intino.ness.box.actions.SortTanksAction action = new io.intino.ness.box.actions.SortTanksAction();
		action.box = box;
		action.execute();
	}
}