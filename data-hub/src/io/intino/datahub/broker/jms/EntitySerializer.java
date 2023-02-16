package io.intino.datahub.broker.jms;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Layer;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.messages.UpdateMasterMessage;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.persistence.MasterTripletWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

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

		private String entityId;

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
			entityId = record.id();
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
			entityId = message.value();
			String serializedRecord = master.masterMap().get(message.value());
			if (serializedRecord == null) return;

			TripletRecord record = master.serializer().deserialize(serializedRecord);

			boolean wasEnabled = "true".equals(record.getValueOrDefault("enabled", "true"));
			if (wasEnabled == enabledNewValue) return;

			record.put(new Triplet(record.id(), "enabled", String.valueOf(enabledNewValue)));
			serializedRecord = master.serializer().serialize(record);
			master.masterMap().put(record.id(), serializedRecord);

			saveToDatalake(List.of(record.getTriplet("enabled")));
		}

		private boolean publishNewOrModifiedTriplets(UpdateMasterMessage message) throws Exception {
			List<Triplet> tripletsToPublish = getNewOrModifiedTriplets(master.serializer().deserialize(message.value()));
			if (tripletsToPublish.isEmpty()) return false;
			setAuthorToTriplets(message.clientName(), tripletsToPublish);
			saveToDatalake(tripletsToPublish);
			return true;
		}

		private void saveToDatalake(List<Triplet> triplets) throws IOException {
			for(String tank : tanksOf()) {
				new MasterTripletWriter(new File(master.datalakeRootPath(), DATALAKE_ENTITIES_SUBDIR)).write(tank, triplets);
			}
		}

		private Iterable<String> tanksOf() {
			String type = Triplet.typeOf(entityId);
			List<String> targets = new ArrayList<>();

			Entity entity = graph.entityList().stream().filter(e -> e.name$().equals(StringUtils.capitalize(type))).findFirst().orElse(null);
			if(entity == null) return emptyList();
			if(entity.isExtensionOf()) addParentEntities(entity.asExtensionOf().entity(), targets);
			targets.add(entity.name$());

			Set<String> tanks = definedTanks();
			targets.removeIf(t -> !tanks.contains(t));

			return targets;
		}

		private Set<String> definedTanks() {
			return graph.datalake().tankList().stream()
					.filter(Datalake.Tank::isEntity)
					.map(Datalake.Tank::asEntity)
					.map(Datalake.Tank.Entity::entity)
					.filter(Objects::nonNull)
					.map(Layer::name$)
					.collect(Collectors.toSet());
		}

		private void addParentEntities(Entity entity, List<String> targets) {
			if(entity == null) return;
			targets.add(entity.name$());
			if(entity.isExtensionOf()) addParentEntities(entity.asExtensionOf().entity(), targets);
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