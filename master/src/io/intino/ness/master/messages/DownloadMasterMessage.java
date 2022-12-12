package io.intino.ness.master.messages;

import io.intino.alexandria.message.Message;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadMasterMessage extends MasterMessage {

	public static final String PROPERTY_ENTITY_SERIALIZER = "entity_serializer";
	public static final String PROPERTY_ERROR = "error";

	public DownloadMasterMessage(Set<String> tanks, EntityFilter filter) {
		if(tanks != null) message.set("tanks", String.join(",", tanks));
		message.set("filter", filter.name());
	}

	public DownloadMasterMessage(Message message) {
		super(message);
	}

	public Set<String> tanks() {
		Message.Value tanks = message.get("tanks");
		if(tanks == null || tanks.asString() == null) return null;
		return Arrays.stream(tanks.asString().split(",")).collect(Collectors.toSet());
	}

	public EntityFilter filter() {
		return EntityFilter.valueOf(message.get("filter").asString());
	}

	public enum EntityFilter {
		OnlyEnabled, OnlyDisabled, AllEntities
	}
}
