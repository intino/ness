package io.intino.ness.box.scheduling;

import org.quartz.*;
import io.intino.ness.box.NessBox;
import io.intino.konos.scheduling.ScheduledTrigger;

public class SealTask implements ScheduledTrigger {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		NessBox box = (NessBox) context.getMergedJobDataMap().get("box");
		io.intino.ness.box.actions.SealAction action = new io.intino.ness.box.actions.SealAction();
		action.box = box;
		action.execute();
	}
}