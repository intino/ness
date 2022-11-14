package io.intino.ness.master.data.update;

import io.intino.ness.master.model.TripletRecord;

import java.time.Instant;

public class MasterPublishError {

	private final String publisherName;
	private final TripletRecord record;
	private final Instant ts;
	private final String errorName;
	private final String errorMessage;

	public MasterPublishError(String publisherName, TripletRecord record, Instant ts, String errorName, String errorMessage) {
		this.publisherName = publisherName;
		this.record = record;
		this.ts = ts;
		this.errorName = errorName;
		this.errorMessage = errorMessage;
	}

	public String publisherName() {
		return publisherName;
	}

	public TripletRecord record() {
		return record;
	}

	public Instant ts() {
		return ts;
	}

	public String errorName() {
		return errorName;
	}

	public String errorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "MasterPublishError{" +
				"publisherName='" + publisherName + '\'' +
				", record=" + record +
				", ts=" + ts +
				", errorName='" + errorName + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
