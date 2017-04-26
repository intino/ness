package io.intino.ness.konos;

import io.intino.ness.konos.NessBox;
import io.intino.konos.scheduling.KonosTasker;
import io.intino.konos.scheduling.directory.KonosDirectorySentinel;
import io.intino.ness.konos.scheduling.*;
import org.quartz.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

public class Tasks {
	private static final Logger logger = Logger.getGlobal();

	private Tasks() {

	}

	public static void init(KonosTasker tasker, NessBox box) {
		JobDetail job;
		try {
			job = newJob(ToTubTask.class).withIdentity("toTub").build();
			job.getJobDataMap().put("box", box);
			tasker.scheduleJob(job, newSet(newTrigger().withIdentity("Model#toTub").withSchedule(cronSchedule("0 0 0 1/1 * ? *")).build()), true);
			tasker.startSchedules();
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
	}

	private static Set<Trigger> newSet(Trigger... triggers) {
		LinkedHashSet<Trigger> set = new LinkedHashSet<>();
		java.util.Collections.addAll(set, triggers);
		return set;
	}
}