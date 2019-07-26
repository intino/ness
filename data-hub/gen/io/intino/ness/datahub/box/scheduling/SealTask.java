package io.intino.ness.datahub.box.scheduling;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import io.intino.ness.datahub.box.DataHubBox;
import io.intino.alexandria.scheduler.ScheduledTrigger;

public class SealTask implements ScheduledTrigger {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		DataHubBox box = (DataHubBox) context.getMergedJobDataMap().get("box");
		io.intino.ness.datahub.box.actions.SealAction action = new io.intino.ness.datahub.box.actions.SealAction();
		action.box = box;
		action.execute();
	}
}