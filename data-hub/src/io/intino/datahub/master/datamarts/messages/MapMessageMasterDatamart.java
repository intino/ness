package io.intino.datahub.master.datamarts.messages;

import io.intino.alexandria.message.Message;
import io.intino.datahub.master.MasterDatamart;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMessageMasterDatamart implements MasterDatamart<Message> {

	private final String name;
	private final Map<String, Message> messages;

	public MapMessageMasterDatamart(String name) {
		this.name = name;
		this.messages = new HashMap<>();
	}

	public MapMessageMasterDatamart(String name, Map<String, Message> messages) {
		this.name = name;
		this.messages = messages;
	}

	@Override
	public String name() {
		return name;
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
		Message oldMessage = messages.get(id);
		if(oldMessage != null)
			update(oldMessage, newMessage);
		else
			messages.put(id, newMessage);
	}

	private void update(Message message, Message changes) {
		for(String attribute : changes.attributes()) {
			message.set(attribute, changes.get(attribute).data());
		}
		// TODO: how to update old components??
		List<Message> components = message.components();
		for(Message newComponent : changes.components()) {
			if(!components.contains(newComponent)) message.add(newComponent);
		}
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
	public Map<String, Message> toMap() {
		return Collections.unmodifiableMap(messages);
	}

	private void fail(MasterDatamart<Message> other, Map.Entry<String, Message> entry, String id) {
		Message message = get(id);
		if(!message.type().equals(entry.getValue().type()))
			throw new IllegalStateException("Failed to merge datamart " + other.name() + " into " + name
					+ ": A message with id '" + id + "' is already present.");
	}
}
