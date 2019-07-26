package io.intino.ness.datahub.box;

import io.intino.ness.datahub.box.DataHubBox;
import io.intino.alexandria.scheduler.AlexandriaScheduler;
import io.intino.ness.datahub.box.scheduling.*;
import org.quartz.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.time.ZoneId;
import java.util.TimeZone;
import io.intino.alexandria.logger.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

public class Tasks {

	private Tasks() {

	}

	public static void init(AlexandriaScheduler tasker, DataHubBox box) {
		JobDetail job;
		try {
			job = newJob(SealTask.class).withIdentity("seal").build();
			job.getJobDataMap().put("box", box);
			tasker.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#seal").withSchedule(cronSchedule("0 0 4 1/1 * ? *")).build()), true);

			tasker.startSchedules();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}

	private static Set<Trigger> newSet(Trigger... triggers) {
		LinkedHashSet<Trigger> set = new LinkedHashSet<>();
		java.util.Collections.addAll(set, triggers);
		return set;
	}
}