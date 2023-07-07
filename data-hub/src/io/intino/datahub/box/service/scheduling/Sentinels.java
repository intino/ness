package io.intino.datahub.box.service.scheduling;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.scheduler.AlexandriaScheduler;
import io.intino.alexandria.scheduler.ScheduledTrigger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.actions.BackupAction;
import io.intino.datahub.box.actions.DatamartsSnapshotAction;
import io.intino.datahub.box.actions.SealAction;
import io.intino.datahub.model.Datalake;
import org.quartz.*;

import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class Sentinels {

	private final AlexandriaScheduler scheduler;

	public Sentinels(DataHubBox box) {
		this.scheduler = new AlexandriaScheduler();
		try {
			if (box.graph().datalake() != null && box.graph().datalake().seal() != null) addSealingSentinel(box);
			if (box.graph().datalake() != null && box.graph().datalake().backup() != null)
				addDatalakeBackupSentinel(box);
			if (box.graph().datamartList() != null && !box.graph().datamartList().isEmpty())
				addDatamartsSnapshotSentinel(box);
			scheduler.startSchedules();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}

	private void addDatamartsSnapshotSentinel(DataHubBox box) throws SchedulerException {
		JobDetail job = newJob(DatamartsSnapshotSentinel.class).withIdentity("DatamartsSnapshot").build();
		job.getJobDataMap().put("box", box);
		scheduler.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#DatamartsSnapshot").withSchedule(cronSchedule(dailyAt00AM()).inTimeZone(TimeZone.getDefault())).build()), true);
	}

	private void addDatalakeBackupSentinel(DataHubBox box) throws SchedulerException {
		JobDetail job = newJob(DatalakeBackupListener.class).withIdentity("DatalakeBackup").build();
		job.getJobDataMap().put("box", box);
		Datalake.Backup.Cron cron = box.graph().datalake().backup().cron();
		String zoneId = cron.timeZone();
		scheduler.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#DatalakeBackup").withSchedule(cronSchedule(cron.pattern()).inTimeZone(zoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(ZoneId.of(zoneId)))).build()), true);
	}

	private void addSealingSentinel(DataHubBox box) throws SchedulerException {
		JobDetail job = newJob(SealingListener.class).withIdentity("Sealing").build();
		job.getJobDataMap().put("box", box);
		Datalake.Seal.Cron cron = box.graph().datalake().seal().cron();
		String zoneId = cron.timeZone();
		scheduler.scheduleJob(job, newSet(newTrigger().withIdentity("DataHub#Sealing").withSchedule(cronSchedule(cron.pattern()).inTimeZone(zoneId == null ? TimeZone.getDefault() : TimeZone.getTimeZone(ZoneId.of(zoneId)))).build()), true);
	}

	public void stop() {
		try {
			scheduler.shutdownSchedules();
		} catch (SchedulerException e) {
			Logger.error(e);
		}
	}

	private String dailyAt00AM() {
		return "0 0 0 1/1 * ? *";
	}

	private static Set<Trigger> newSet(Trigger... triggers) {
		LinkedHashSet<Trigger> set = new LinkedHashSet<>();
		java.util.Collections.addAll(set, triggers);
		return set;
	}

	public static class DatalakeBackupListener implements ScheduledTrigger {
		public void execute(JobExecutionContext context) {
			new BackupAction((DataHubBox) context.getMergedJobDataMap().get("box")).execute();
		}
	}

	public static class SealingListener implements ScheduledTrigger {
		public void execute(JobExecutionContext context) {
			new SealAction((DataHubBox) context.getMergedJobDataMap().get("box")).execute();
		}
	}

	public static class DatamartsSnapshotSentinel implements ScheduledTrigger {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			new DatamartsSnapshotAction((DataHubBox) context.getMergedJobDataMap().get("box")).execute();
		}
	}
}