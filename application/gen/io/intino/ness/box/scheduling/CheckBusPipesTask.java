package io.intino.ness.box.scheduling;

import org.quartz.*;
import io.intino.ness.box.NessBox;
import io.intino.konos.scheduling.ScheduledTrigger;

public class CheckBusPipesTask implements ScheduledTrigger {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		NessBox box = (NessBox) context.getMergedJobDataMap().get("box");
		io.intino.ness.box.actions.CheckBusPipesAction action = new io.intino.ness.box.actions.CheckBusPipesAction();
		action.box = box;
		action.execute();
	}
}