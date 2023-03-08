package io.intino.datahub.datamart.messages;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.serialization.MasterDatamartSnapshots;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.NessGraph;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;
import static io.intino.datahub.datamart.serialization.MasterDatamartSnapshots.saveSnapshot;

public class MessageMasterDatamartFactory {

	private final NessGraph graph;
	private final File datamartsRoot;
	private final Datalake datalake;

	public MessageMasterDatamartFactory(NessGraph graph, File datamartsRoot, Datalake datalake) {
		this.graph = graph;
		this.datamartsRoot = datamartsRoot;
		this.datalake = datalake;
	}

	public MasterDatamart<Message> create(Datamart definition) throws IOException {
		Optional<MasterDatamart.Snapshot<Message>> snapshot = MasterDatamartSnapshots.loadMostRecentSnapshot(datamartsRoot, definition.name$(), graph);

		MasterDatamart<Message> datamart;
		Timetag fromTimetag;

		if (snapshot.isPresent()) {
			datamart = snapshot.get().datamart();
			fromTimetag = snapshot.get().timetag();
		} else {
			datamart = new MapMessageMasterDatamart(definition);
			fromTimetag = null;
		}

		reflow(datamart, fromTimetag, definition);

		return datamart;
	}

	private void reflow(MasterDatamart<Message> datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		MasterDatamartMessageMounter mounter = new MasterDatamartMessageMounter(datamart);
		Iterator<MessageEvent> iterator = reflowEntityTanksFrom(fromTimetag, definition);
		while (iterator.hasNext()) {
			MessageEvent event = iterator.next();
			Timetag timetag = Timetag.of(event.ts(), Scale.Day);
			if (shouldCreateSnapshot(timetag, definition.scale())) saveSnapshot(datamartsRoot, timetag, datamart);
			mounter.mount(event.toMessage());
		}
	}

	private Iterator<MessageEvent> reflowEntityTanksFrom(Timetag fromTimetag, Datamart definition) {
		return definition.entityList().stream()
				.filter(e -> e.from() != null)
				.map(e -> datalake.messageStore().tank(e.from().name$()))
				.flatMap(t -> fromTimetag == null ? t.content() : t.content((ss, ts) -> !ts.isBefore(fromTimetag)))
				.iterator();
	}
}
