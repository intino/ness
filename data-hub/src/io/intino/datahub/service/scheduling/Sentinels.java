package io.intino.datahub.service.scheduling;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.scheduler.AlexandriaScheduler;
import io.intino.alexandria.scheduler.ScheduledTrigger;
import io.intino.datahub.DataHub;
import io.intino.datahub.datalake.actions.DatalakeBackupAction;
import io.intino.datahub.datalake.actions.SealingAction;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class Sentinels {

	private final AlexandriaScheduler scheduler;

	public Sentinels(DataHub dataHub) {
		this.scheduler = new AlexandriaScheduler();
		JobDetail job;
		try {
			if (dataHub.graph().datalake() != null && dataHub.graph().datalake().seal() != null) {
				job = newJob(SealingListener.class).withIdentity("Sealing").build();
				job.getJobDataMap().put("dataHub", dataHub);
				scheduler.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#Sealing").withSchedule(cronSchedule(dataHub.graph().datalake().seal().cronPattern()).inTimeZone(TimeZone.getTimeZone(ZoneId.of("Mexico/General")))).build(), newTrigger().startNow().build()), true);
			}
			if (dataHub.graph().datalake() != null && dataHub.graph().datalake().backup() != null) {
				job = newJob(DatalakeBackupListener.class).withIdentity("DatalakeBackup").build();
				job.getJobDataMap().put("dataHub", dataHub);
				scheduler.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#DatalakeBackup").withSchedule(cronSchedule(dataHub.graph().datalake().backup().cronPattern()).inTimeZone(TimeZone.getTimeZone(ZoneId.of("Mexico/General")))).build()), true);
			}
			scheduler.startSchedules();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}

	public void stop() {
		try {
			scheduler.shutdownSchedules();
		} catch (SchedulerException e) {
			Logger.error(e);
		}
	}

	public static class DatalakeBackupListener implements ScheduledTrigger {
		public void execute(JobExecutionContext context) {
			new DatalakeBackupAction((DataHub) context.getMergedJobDataMap().get("dataHub")).execute();
		}
	}

	public static class SealingListener implements ScheduledTrigger {
		public void execute(JobExecutionContext context) {
			new SealingAction((DataHub) context.getMergedJobDataMap().get("dataHub")).execute();
		}
	}

	private static Set<Trigger> newSet(Trigger... triggers) {
		LinkedHashSet<Trigger> set = new LinkedHashSet<>();
		java.util.Collections.addAll(set, triggers);
		return set;
	}
}