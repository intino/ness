package systems.intino.eventsourcing.datahub.datamart;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.impl.LocalDatamart;
import systems.intino.eventsourcing.datahub.datamart.mounters.SubjectMounter.Batch;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.Subject;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.event.EventStream;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			Set<systems.intino.eventsourcing.datahub.model.Datalake.Tank.Message> subjectTanks = DatamartUtils.subjectTanks(datamart, subject);
			try (Batch mounter = new Batch(datamart, subjectTanks)) {
				DatamartUtils.messageTanksOf(datalake, datamart.definition(), subject).stream()
						.flatMap(Datalake.Store.Tank::sources)
						.collect(Collectors.groupingBy(Datalake.Store.Source::name))
						.forEach((ss, events) -> EventStream.merge(events.stream().map(Datalake.Store.Source::content)).forEach(mounter::mount));
			} catch (Exception e) {
				Logger.error(e);
			}
		}
	}

	private void removeAllSubjectFiles(MasterDatamart datamart) throws IOException {
		FileUtils.deleteDirectory(box.datamartSubjectsDirectory(datamart.name()));
		FileUtils.deleteDirectory(box.datamartIndicatorsDirectory(datamart.name()));
	}
}