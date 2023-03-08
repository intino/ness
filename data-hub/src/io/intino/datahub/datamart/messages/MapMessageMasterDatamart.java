package io.intino.datahub.datamart.messages;

import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapMessageMasterDatamart implements MasterDatamart<Message> {

	private final Datamart definition;
	private final Map<String, Message> messages;
	private final Set<String> subscribedEvents;

	public MapMessageMasterDatamart(Datamart definition) {
		this(definition, new HashMap<>(128));
	}

	public MapMessageMasterDatamart(Datamart definition, Map<String, Message> messages) {
		this.definition = definition;
		this.messages = Collections.synchronizedMap(messages);
		this.subscribedEvents = definition.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull)
				.map(m -> m.message().name$())
				.collect(Collectors.toSet());
	}

	@Override
	public String name() {
		return definition.name$();
	}

	@Override
	public int size() {
		return messages.size();
	}

	@Override
	public boolean contains(String id) {
		return messages.containsKey(id);
	}

	@Override
	public Message get(String id) {
		return messages.get(id);
	}

	@Override
	public void put(String id, Message newMessage) {
		messages.put(id, newMessage);
	}

	@Override
	public void putAll(MasterDatamart<Message> other) {
		for(Map.Entry<String, Message> entry : other.toMap().entrySet()) {
			String id = entry.getKey();
			if(contains(id)) fail(other, entry, id);
			put(id, entry.getValue());
		}
	}

	@Override
	public void remove(String id) {
		messages.remove(id);
	}

	@Override
	public void clear() {
		messages.clear();
	}

	@Override
	public Stream<Message> elements() {
		return messages.values().stream();
	}

	@Override
	public Map<String, Message> toMap() {
		return Collections.unmodifiableMap(messages);
	}

	@Override
	public Class<Message> elementType() {
		return Message.class;
	}

	@Override
	public Collection<String> subscribedEvents() {
		return subscribedEvents;
	}

	private void fail(MasterDatamart<Message> other, Map.Entry<String, Message> entry, String id) {
		Message message = get(id);
		if(!message.type().equals(entry.getValue().type()))
			throw new IllegalStateException("Failed to merge datamart " + other.name() + " into " + name()
					+ ": A message with id '" + id + "' is already present.");
	}
}
