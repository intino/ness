package io.intino.ness.master.data.update;

import io.intino.ness.master.core.Master;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class MasterMessageHandler {

	private final Master master;

	public MasterMessageHandler(Master master) {
		this.master = master;
	}

	public void handle(MasterMessage message) throws Exception {
		if(publishNewOrModifiedTriplets(message))
			updateMasterMap(message);
	}

	private boolean publishNewOrModifiedTriplets(MasterMessage message) throws Exception {
		List<Triplet> tripletsToPublish = getNewOrModifiedTriplets(message.record);
		if(tripletsToPublish.isEmpty()) return false;
		new MasterPublisher(new File(master.datalakeRootPath(), "triplets")).publish(tripletsToPublish);
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

	private void updateMasterMap(MasterMessage message) {
		synchronized (MasterMessageHandler.class) {
			master.masterMap().set(message.record.id(), message.serializedRecord);
		}
	}

	public static class MasterMessage {

		public final TripletRecord record;
		public final String serializedRecord;
		public final String publisherName;
		public final Instant ts;

		public MasterMessage(TripletRecord record, String serializedRecord, String publisherName, Instant ts) {
			this.record = record;
			this.serializedRecord = serializedRecord;
			this.publisherName = publisherName;
			this.ts = ts;
		}
	}
}
