package systems.intino.eventsourcing.datahub.datamart;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.impl.LocalDatamart;
import systems.intino.eventsourcing.datahub.datamart.mounters.SubjectMounter.Batch;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.Subject;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.datalake.Datalake.Store.Source;
import systems.intino.eventsourcing.datalake.Datalake.Store.Tank;
import systems.intino.eventsourcing.event.EventStream;
import systems.intino.eventsourcing.event.message.MessageEvent;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static systems.intino.eventsourcing.datahub.datamart.DatamartUtils.tankName;

public class DatamartFactory {
	private final DatahubBox box;
	private final Datalake datalake;

	public DatamartFactory(DatahubBox box, Datalake datalake) {
		this.box = box;
		this.datalake = datalake;
	}

	public MasterDatamart create(Datamart definition) throws Exception {
		return reflow(new LocalDatamart(box, definition), definition);
	}

	public MasterDatamart reflow(MasterDatamart datamart, Datamart model) throws Exception {
		removeAllSubjectFiles(datamart);
		reflowSubjects(datamart, model.subjectList());
		Logger.debug("Reflow complete");
		//TODO save snapshot
		return datamart;
	}

	private void reflowSubjects(MasterDatamart datamart, List<Subject> subjects) {
		Logger.debug("Reflowing subjects...");
		for (Subject subject : subjects) {
			if (subject.isAbstract()) continue;
			Batch mounter = new Batch(datamart, subject.name$(), subject.from());
			var map = subject.from().stream().map(message -> datalake.messageStore().tank(tankName(message)))
					.flatMap(Tank::sources)
					.collect(groupingBy(Source::name));
			map.forEach((ss, sources) -> mount(sources, mounter));
		}
	}

	private static void mount(List<Source<MessageEvent>> events, Batch mounter) {
		try (mounter) {
			EventStream.merge(events.stream().map(Source::content))
					.forEach(mounter::mount);
		} catch (Exception e) {
			Logger.error(mounter.subject() + ": " + e.getMessage());
		}
	}

	private void removeAllSubjectFiles(MasterDatamart datamart) throws IOException {
		FileUtils.deleteDirectory(box.datamartSubjectsDirectory(datamart.name()));
		FileUtils.deleteDirectory(box.datamartIndicatorsDirectory(datamart.name()));
	}
}