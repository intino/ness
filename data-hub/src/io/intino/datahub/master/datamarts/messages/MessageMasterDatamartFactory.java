package io.intino.datahub.master.datamarts.messages;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.datahub.master.MasterDatamart;
import io.intino.datahub.master.serialization.MasterDatamartSnapshots;
import io.intino.datahub.model.Datamart;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static io.intino.datahub.master.MasterDatamart.Snapshot.shouldCreateSnapshot;
import static io.intino.datahub.master.serialization.MasterDatamartSnapshots.loadMostRecentSnapshot;

// TODO: reflow all entities, only load enabled entities or maintain 2 different versions of each datamart (enabled/disabled)?
public class MessageMasterDatamartFactory {

	private final File datamartsRoot;
	private final Datalake datalake;

	public MessageMasterDatamartFactory(File datamartsRoot, Datalake datalake) {
		this.datamartsRoot = datamartsRoot;
		this.datalake = datalake;
	}

	public MasterDatamart<Message> create(Datamart definition) throws IOException {
		Optional<MasterDatamart.Snapshot<Message>> snapshot = loadMostRecentSnapshot(datamartsRoot, definition.name$());

		MasterDatamart<Message> datamart;
		Timetag fromTimetag;

		if (snapshot.isPresent()) {
			datamart = snapshot.get().datamart();
			fromTimetag = snapshot.get().timetag();
		} else {
			datamart = new MapMessageMasterDatamart(definition.name$());
			fromTimetag = null;
		}

		reflow(datamart, fromTimetag, definition);

		return datamart;
	}

	private void reflow(MasterDatamart<Message> datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		Iterator<MessageEvent> iterator = reflowEntityTanksFrom(fromTimetag, definition);
		while (iterator.hasNext()) {
			MessageEvent event = iterator.next();
			Timetag timetag = Timetag.of(event.ts(), Scale.Day);
			if (shouldCreateSnapshot(timetag, definition.scale()))
				MasterDatamartSnapshots.save(datamartsRoot, timetag, datamart);
			mount(event, datamart);
		}
	}

	private Iterator<MessageEvent> reflowEntityTanksFrom(Timetag fromTimetag, Datamart definition) {
		return definition.entityList().stream()
				.map(e -> datalake.messageStore().tank(e.from().name$()))
				.flatMap(t -> fromTimetag == null ? t.content() : t.content((ss, ts) -> !ts.isBefore(fromTimetag)))
				.iterator();
	}

	private void mount(MessageEvent event, MasterDatamart<Message> datamart) {
		Message message = event.toMessage();
		String id = message.get("id").asString();
		if (id == null || id.isEmpty()) return;
		datamart.put(id, message);
	}
}
