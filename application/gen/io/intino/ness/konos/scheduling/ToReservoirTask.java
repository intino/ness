package io.intino.ness.konos.scheduling;

import org.quartz.*;
import io.intino.ness.konos.NessBox;
import io.intino.konos.scheduling.ScheduledTrigger;

public class ToReservoirTask implements ScheduledTrigger {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		NessBox box = (NessBox) context.getMergedJobDataMap().get("box");
		io.intino.ness.konos.actions.ToReservoirAction action = new io.intino.ness.konos.actions.ToReservoirAction();
		action.box = box;
		action.execute();
	}
}