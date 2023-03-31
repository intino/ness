package io.intino.datahub.datamart.messages;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.serialization.MasterDatamartSerializer;
import io.intino.datahub.datamart.serialization.MasterDatamartSnapshots;
import io.intino.datahub.model.Datamart;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;
import static io.intino.datahub.datamart.serialization.MasterDatamartSnapshots.saveSnapshot;

public class MessageMasterDatamartFactory {

	private final DataHubBox box;
	private final File datamartsRoot;
	private final Datalake datalake;

	public MessageMasterDatamartFactory(DataHubBox box, File datamartsRoot, Datalake datalake) {
		this.box = box;
		this.datamartsRoot = datamartsRoot;
		this.datalake = datalake;
	}

	public MasterDatamart<Message> create(Datamart definition) throws IOException {
		Reference<MasterDatamart<Message>> datamart = new Reference<>();
		Reference<Timetag> fromTimetag = new Reference<>();

		if(failedToLoadLastBackupOf(definition, datamart, fromTimetag)) {
			if (failedToLoadLastSnapshotOf(definition, datamart, fromTimetag)) {
				datamart.value = new MapMessageMasterDatamart(definition);
				fromTimetag.value = null;
			}
			reflow(datamart.value, fromTimetag.value, definition);
		}

		return datamart.value;
	}

	private boolean failedToLoadLastBackupOf(Datamart definition, Reference<MasterDatamart<Message>> datamart, Reference<Timetag> fromTimetag) {
		File backup = MasterDatamartSerializer.backupFileOf(definition, box);
		if(!backup.exists()) return true;
		try {
			datamart.value = MasterDatamartSerializer.deserialize(backup, definition);
		} catch (IOException e) {
			Logger.error("Could not deserialize datamart " + definition.name$() + " from " + backup + ": " + e.getMessage(), e);
			return true;
		}
		return false;
	}

	private boolean failedToLoadLastSnapshotOf(Datamart definition, Reference<MasterDatamart<Message>> datamart, Reference<Timetag> fromTimetag) {
		Optional<MasterDatamart.Snapshot<Message>> snapshot = MasterDatamartSnapshots.loadMostRecentSnapshot(datamartsRoot, definition.name$(), box.graph());
		if(snapshot.isPresent()) {
			datamart.value = snapshot.get().datamart();
			fromTimetag.value = snapshot.get().timetag();
			return false;
		}
		return true;
	}

	private void reflow(MasterDatamart<Message> datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		MasterDatamartMessageMounter mounter = new MasterDatamartMessageMounter(datamart);
		Iterator<MessageEvent> iterator = reflowEntityTanksFrom(fromTimetag, definition);
		int count = 0;
		while (iterator.hasNext()) {
			MessageEvent event = iterator.next();
			Timetag timetag = Timetag.of(event.ts(), Scale.Day);
			if (shouldCreateSnapshot(timetag, definition.scale(), definition.firstDayOfWeek())) saveSnapshot(datamartsRoot, timetag, datamart);
			mounter.mount(event.toMessage());
			++count;
		}
		Logger.info("Reflow finished for datamart " + datamart.name() + " (events = " + count + ")");
	}

	private Iterator<MessageEvent> reflowEntityTanksFrom(Timetag fromTimetag, Datamart definition) {
		return definition.entityList().stream()
				.filter(e -> e.from() != null)
				.map(e -> datalake.messageStore().tank(e.from().message().core$().fullName().replace("$", ".")))
				.flatMap(t -> fromTimetag == null ? t.content() : t.content((ss, ts) -> !ts.isBefore(fromTimetag)))
				.iterator();
	}

	private static class Reference<T> {
		public T value;
	}
}
