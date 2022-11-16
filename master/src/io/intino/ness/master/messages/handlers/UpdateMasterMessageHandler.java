package io.intino.ness.master.messages.handlers;

import io.intino.ness.master.core.Master;
import io.intino.ness.master.messages.ListenerMasterMessage;
import io.intino.ness.master.messages.ListenerMasterMessage.Action;
import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.MasterMessagePublisher;
import io.intino.ness.master.messages.UpdateMasterMessage;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.persistence.MasterTripletWriter;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.ness.master.messages.MasterTopics.MASTER_LISTENER_TOPIC;

public class UpdateMasterMessageHandler implements MasterMessageHandler<UpdateMasterMessage> {

	private final Master master;

	public UpdateMasterMessageHandler(Master master) {
		this.master = master;
	}

	@Override
	public Class<UpdateMasterMessage> messageClass() {
		return UpdateMasterMessage.class;
	}

	@Override
	public void handle(UpdateMasterMessage message) throws MasterMessageException {
		try {
			synchronized (UpdateMasterMessageHandler.class) {
				switch(message.action()) {
					case Publish: handlePublish(message); break;
					case Enable: handleEnable(message); break;
					case Disable: handleDisable(message); break;
					case Remove: handleRemove(message); break;
				}
			}
		} catch (Throwable e) {
			throw new MasterMessageException("Error while processing the UpdateMasterMessage " + message.id(), e).originalMessage(message);
		}
	}

	private void handlePublish(UpdateMasterMessage message) throws Exception {
		if(publishNewOrModifiedTriplets(message)) {
			TripletRecord record = master.serializer().deserialize(message.value());
			boolean wasAlreadyCreated = master.masterMap().containsKey(record.id());
			master.masterMap().set(record.id(), message.value());
			publishListenerMessage(message.author(), wasAlreadyCreated ? Action.Updated : Action.Created, message.id(), message.value());
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
		if(serializedRecord == null) return;

		TripletRecord record = master.serializer().deserialize(serializedRecord);

		boolean wasEnabled = "true".equals(record.getValueOrDefault("enabled", "true"));
		if(wasEnabled == enabledNewValue) return;

		record.put(new Triplet(record.id(), "enabled", String.valueOf(enabledNewValue)));
		serializedRecord = master.serializer().serialize(record);
		master.masterMap().set(record.id(), serializedRecord); // TODO: save enabled triplet

		new MasterTripletWriter(new File(master.datalakeRootPath(), "triplets")).publish(List.of(record.getTriplet("enabled")));

		publishListenerMessage(message.author(), enabledNewValue ? Action.Enabled : Action.Disabled, message.id(), serializedRecord);
	}

	private void handleRemove(UpdateMasterMessage message) {
		if(!master.masterMap().containsKey(message.value())) return;
		String removedRecord = master.masterMap().remove(message.value());
		publishListenerMessage(message.author(), Action.Removed, message.id(), removedRecord);
	}

	private void publishListenerMessage(String author, Action action, String updateMessageId, String record) {
		MasterMessagePublisher.publishMessage(master, MASTER_LISTENER_TOPIC, new ListenerMasterMessage(
				author,
				action,
				updateMessageId,
				record,
				Instant.now()));
	}

	private boolean publishNewOrModifiedTriplets(UpdateMasterMessage message) throws Exception {
		List<Triplet> tripletsToPublish = getNewOrModifiedTriplets(master.serializer().deserialize(message.value()));
		if(tripletsToPublish.isEmpty()) return false;
		new MasterTripletWriter(new File(master.datalakeRootPath(), "triplets")).publish(tripletsToPublish);
		return true;
	}

	// TODO: handle removal of attributes. Special care when dealing with primitive and non-nullable values
	private List<Triplet> getNewOrModifiedTriplets(TripletRecord newRecord) {
		List<Triplet> triplets = newRecord.triplets().collect(Collectors.toList());
		if(!master.masterMap().containsKey(newRecord.id())) return triplets;
		TripletRecord oldRecord = master.serializer().deserialize(master.masterMap().get(newRecord.id()));
		triplets.removeIf(oldRecord::contains);
		return triplets;
	}
}
