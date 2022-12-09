//package io.intino.ness.master.messages.handlers;
//
//import io.intino.ness.master.core.Master;
//import io.intino.ness.master.messages.MasterMessageException;
//import io.intino.ness.master.messages.UpdateMasterMessage;
//import io.intino.ness.master.model.Triplet;
//import io.intino.ness.master.model.TripletRecord;
//import io.intino.ness.master.persistence.MasterTripletWriter;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static io.intino.ness.master.messages.MasterTopics.MASTER_LISTENER_TOPIC;
//
//@Deprecated
//public class UpdateMasterMessageHandler implements MasterMessageHandler<UpdateMasterMessage> {
//
//	private final Master master;
//
//	public UpdateMasterMessageHandler(Master master) {
//		this.master = master;
//	}
//
//	@Override
//	public Class<UpdateMasterMessage> messageClass() {
//		return UpdateMasterMessage.class;
//	}
//
//	@Override
//	public void handle(UpdateMasterMessage message) throws MasterMessageException {
//		try {
//			synchronized (UpdateMasterMessageHandler.class) {
//				switch(message.action()) {
//					case Publish: handlePublish(message); break;
//					case Enable: handleEnable(message); break;
//					case Disable: handleDisable(message); break;
//					case Remove: handleRemove(message); break;
//				}
//			}
//		} catch (Throwable e) {
//			throw new MasterMessageException("Error while processing the UpdateMasterMessage " + message.id(), e)
//					.clientName(message.clientName())
//					.originalMessage(message);
//		}
//	}
//
//	private void handlePublish(UpdateMasterMessage message) throws Exception {
//		TripletRecord record = master.serializer().deserialize(message.value());
//		if(publishNewOrModifiedTriplets(message)) {
//			boolean wasAlreadyCreated = master.masterMap().containsKey(record.id());
//			master.masterMap().put(record.id(), message.value());
//			publishListenerMessage(message.clientName(), wasAlreadyCreated ? Action.Updated : Action.Created, message.id(), record.id(), message.value());
//		} else {
//			publishListenerMessage(message.clientName(), Action.None, message.id(), record.id(), message.value());
//		}
//	}
//
//	private void handleEnable(UpdateMasterMessage message) throws IOException {
//		if(!setEnableOrDisable(message, true))
//			publishListenerMessage(message.clientName(), Action.None, message.id(), message.value(), null);
//	}
//
//	private void handleDisable(UpdateMasterMessage message) throws IOException {
//		if(!setEnableOrDisable(message, false))
//			publishListenerMessage(message.clientName(), Action.None, message.id(), message.value(), null);
//	}
//
//	private boolean setEnableOrDisable(UpdateMasterMessage message, boolean enabledNewValue) throws IOException {
//		String serializedRecord = master.masterMap().get(message.value());
//		if(serializedRecord == null) return false;
//
//		TripletRecord record = master.serializer().deserialize(serializedRecord);
//
//		boolean wasEnabled = "true".equals(record.getValueOrDefault("enabled", "true"));
//		if(wasEnabled == enabledNewValue) return false;
//
//		record.put(new Triplet(record.id(), "enabled", String.valueOf(enabledNewValue)));
//		serializedRecord = master.serializer().serialize(record);
//		master.masterMap().put(record.id(), serializedRecord);
//
//		new MasterTripletWriter(new File(master.datalakeRootPath(), "triplets")).write(List.of(record.getTriplet("enabled")));
//
//		publishListenerMessage(message.clientName(), enabledNewValue ? Action.Enabled : Action.Disabled, message.id(), record.id(), serializedRecord);
//
//		return true;
//	}
//
//	private void handleRemove(UpdateMasterMessage message) {
//		String recordId = message.value();
//		if(!master.masterMap().containsKey(recordId)) {
//			publishListenerMessage(message.clientName(), Action.None, message.id(), recordId, null);
//			return;
//		}
//		String removedRecord = master.masterMap().remove(recordId);
//		publishListenerMessage(message.clientName(), Action.Removed, message.id(), recordId, removedRecord);
//	}
//
//	private void publishListenerMessage(String clientName, Action action, String updateMessageId, String recordId, String record) {
//		MasterMessagePublisher.publishMessage(master.hazelcast(), MASTER_LISTENER_TOPIC, new ListenerMasterMessage(
//				"",
//				clientName,
//				action,
//				updateMessageId,
//				recordId,
//				record));
//	}
//
//	private boolean publishNewOrModifiedTriplets(UpdateMasterMessage message) throws Exception {
//		List<Triplet> tripletsToPublish = getNewOrModifiedTriplets(master.serializer().deserialize(message.value()));
//		if(tripletsToPublish.isEmpty()) return false;
//		setAuthorToTriplets(message.clientName(), tripletsToPublish);
//		new MasterTripletWriter(new File(master.datalakeRootPath(), "triplets")).write(tripletsToPublish);
//		return true;
//	}
//
//	private List<Triplet> getNewOrModifiedTriplets(TripletRecord newRecord) {
//		List<Triplet> triplets = newRecord.triplets().collect(Collectors.toList());
//		if(!master.masterMap().containsKey(newRecord.id())) return triplets;
//		TripletRecord oldRecord = master.serializer().deserialize(master.masterMap().get(newRecord.id()));
//		triplets.removeIf(oldRecord::contains);
//		return triplets;
//	}
//
//	private void setAuthorToTriplets(String clientName, List<Triplet> triplets) {
//		if(clientName == null) return;
//		int size = triplets.size();
//		for(int i = 0; i < size; i++) {
//			Triplet t = triplets.get(i);
//			if(t.author() == null)
//				triplets.set(i, Triplet.withAuthor(t, clientName));
//		}
//	}
//}
