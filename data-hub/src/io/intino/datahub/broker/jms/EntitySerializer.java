package io.intino.datahub.broker.jms;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.model.NessGraph;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.messages.UpdateMasterMessage;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.persistence.MasterTripletWriter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class EntitySerializer {

	private static final String DATALAKE_ENTITIES_SUBDIR = "entities";

	private final FileDatalake datalake;
	private final NessGraph graph;
	private final Master master;

	public EntitySerializer(FileDatalake datalake, NessGraph graph, Master master) {
		this.datalake = datalake;
		this.graph = graph;
		this.master = master;
	}

	Consumer<javax.jms.Message> create() {
		return message -> {
			try {
				save(MessageTranslator.toInlMessages(message));
			} catch (Exception e) {
				Logger.error(e);
			}
		};
	}

	private void save(Iterator<Message> messages) {
		messages.forEachRemaining(this::save);
	}

	private void save(Message message) {
		try {
			new MasterMessageHandler().handle(message);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	public class MasterMessageHandler {

		public void handle(Message rawMessage) throws Exception {
			if (!UpdateMasterMessage.class.getName().equals(rawMessage.get("messageClass").asString())) return;
			UpdateMasterMessage message = new UpdateMasterMessage(rawMessage);
			synchronized (MasterMessageHandler.class) {
				switch (message.intent()) {
					case Publish:
						handlePublish(message);
						break;
					case Enable:
						handleEnable(message);
						break;
					case Disable:
						handleDisable(message);
						break;
				}
			}
		}

		private void handlePublish(UpdateMasterMessage message) throws Exception {
			TripletRecord record = master.serializer().deserialize(message.value());
			if (publishNewOrModifiedTriplets(message)) {
				master.masterMap().put(record.id(), message.value());
			}
		}

		private void handleEnable(UpdateMasterMessage message) throws IOException {
			setEnableOrDisable(message, true);
		}

		private void handleDisable(UpdateMasterMessage message) throws IOException {
			setEnableOrDisable(message, false);
		}

		private void setEnableOrDisable(UpdateMasterMessage message, boolean enabledNewValue) throws IOException {
			String serializedRecord = master.masterMap().get(message.value());
			if (serializedRecord == null) return;

			TripletRecord record = master.serializer().deserialize(serializedRecord);

			boolean wasEnabled = "true".equals(record.getValueOrDefault("enabled", "true"));
			if (wasEnabled == enabledNewValue) return;

			record.put(new Triplet(record.id(), "enabled", String.valueOf(enabledNewValue)));
			serializedRecord = master.serializer().serialize(record);
			master.masterMap().put(record.id(), serializedRecord);

			new MasterTripletWriter(new File(datalake.root(), DATALAKE_ENTITIES_SUBDIR)).write(List.of(record.getTriplet("enabled")));
		}

		private boolean publishNewOrModifiedTriplets(UpdateMasterMessage message) throws Exception {
			List<Triplet> tripletsToPublish = getNewOrModifiedTriplets(master.serializer().deserialize(message.value()));
			if (tripletsToPublish.isEmpty()) return false;
			setAuthorToTriplets(message.clientName(), tripletsToPublish);
			new MasterTripletWriter(new File(master.datalakeRootPath(), DATALAKE_ENTITIES_SUBDIR)).write(tripletsToPublish);
			return true;
		}

		private List<Triplet> getNewOrModifiedTriplets(TripletRecord newRecord) {
			List<Triplet> triplets = newRecord.triplets().collect(Collectors.toList());
			if (!master.masterMap().containsKey(newRecord.id())) return triplets;
			TripletRecord oldRecord = master.serializer().deserialize(master.masterMap().get(newRecord.id()));
			triplets.removeIf(oldRecord::contains);
			return triplets;
		}

		private void setAuthorToTriplets(String clientName, List<Triplet> triplets) {
			if (clientName == null) return;
			int size = triplets.size();
			for (int i = 0; i < size; i++) {
				Triplet t = triplets.get(i);
				if (t.author() == null)
					triplets.set(i, Triplet.withAuthor(t, clientName));
			}
		}
	}
}