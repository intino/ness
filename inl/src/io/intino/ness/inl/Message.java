package io.intino.ness.inl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.intino.ness.inl.Deserializer.deserialize;

public class Message {
	private String type;
	private Message owner;
	private Map<String, Attribute> attributes;
	List<Message> components;
	private Map<String, Attachment> attachments;

	public Message(String type) {
		this.type = type;
		this.owner = null;
		this.attributes = new LinkedHashMap<>();
		this.attachments = new HashMap<>();
		this.components = null;
	}

	Message(String type, Message owner) {
		this.type = type;
		this.owner = owner;
		this.attributes = new LinkedHashMap<>();
		this.attachments = new HashMap<>();
		this.components = new ArrayList<>();
	}

	public String type() {
		return type;
	}

	public boolean is(String type) {
		return type.equalsIgnoreCase(this.type);
	}

	public <T> T as(Class<T> type) {
		return deserialize(toString()).next(type);
	}

	public void type(String type) {
		this.type = type;
	}

	public String ts() {
		return get("ts");
	}

	public void ts(String ts) {
		set("ts", ts);
	}

	public String get(String attribute) {
		return use(attribute).value;
	}

	public Value read(final String attribute) {
		return new Value() {
			@Override @SuppressWarnings("unchecked")
			public <T> T as(Class<T> type) {
				String value = use(attribute).value;
				return value != null ? (T) Parsers.get(type).parse(value) : null;
			}
		};
	}

	public Message set(String attribute, String value) {
		if (value == null) return remove(attribute);
		use(attribute).value = value;
		return this;
	}

	public Message set(String attribute, Boolean value) {
		return set(attribute, value.toString());
	}

	public Message set(String attribute, Integer value) {
		return set(attribute, value.toString());
	}

	public Message set(String attribute, Double value) {
		return set(attribute, value.toString());
	}

	public Message set(String attribute, String type, InputStream is) {
		return set(attribute, type, contentOf(is));
	}

	public Message set(String attribute, String type, byte[] content) {
		if (attributes.containsKey(attribute)) detach(get(attribute));
		return set(attribute, "@" + attach(type, content));
	}

	public Message write(String attribute, String value) {
		if (value == null) return this;
		Attribute a = use(attribute);
		a.value = a.value == null ? value : a.value + "\n" + value;
		return this;
	}

	public Message write(String attribute, Boolean value) {
		return write(attribute, value.toString());
	}

	public Message write(String attribute, Integer value) {
		return write(attribute, value.toString());
	}

	public Message write(String attribute, Double value) {
		return write(attribute, value.toString());
	}

	public Message write(String attribute, String type, InputStream is) {
		return write(attribute, type, contentOf(is));
	}

	public Message write(String attribute, String type, byte[] content) {
		return write(attribute, "@" + attach(type, content));
	}

	private String attach(String type, byte[] bytes) {
		String id = UUID.randomUUID().toString() + "." + type;
		attachments.put(id.toLowerCase(), new Attachment(id, bytes));
		return id;
	}

	private Message attach(List<String> ids) {
		for (String id : ids)
			attachments.put(id.toLowerCase(), new Attachment(id,new byte[0]));
		return this;
	}

	private void detach(String ids) {
		for (String id : ids.split("\n"))
			if (id.contains("@")) attachments.remove(id.substring(1));
	}


	public List<Attachment> attachments() {
		return new ArrayList<>(attachments.values());
	}


	private byte[] contentOf(InputStream is) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			int length;
			byte[] data = new byte[16384];
			while ((length = is.read(data, 0, data.length)) != -1)
				os.write(data, 0, length);
			os.flush();
			return os.toByteArray();

		} catch (IOException e) {
			return new byte[0];
		}
	}

	public Message rename(String attribute, String newName) {
		use(attribute).name = newName;
		add(use(attribute));
		remove(attribute);
		return this;
	}

	public Message remove(String attribute) {
		attributes.remove(attribute.toLowerCase());
		return this;
	}


	private Attribute use(String attribute) {
		if (!contains(attribute)) add(new Attribute(attribute));
		return attributes.get(attribute.toLowerCase());
	}

	private void add(Attribute attribute) {
		attributes.put(attribute.name.toLowerCase(), attribute);
	}

	public List<Message> components(String type) {
		List<Message> result = new ArrayList<>();
		if (components == null) return result;
		for (Message component : components)
			if (component.is(type)) result.add(component);
		return result;
	}

	public void add(Message component) {
		if (components == null) components = new ArrayList<>();
		components.add(component);
		component.owner = this;
	}

	public void add(List<Message> components) {
		if (components == null) return;
		for (Message component : components) add(component);
	}

	public void remove(Message component) {
		components.remove(component);
	}


	public void remove(List<Message> components) {
		this.components.removeAll(components);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("[" + path() + "]");
		for (Attribute attribute : attributes.values()) result.append("\n").append(stringOf(attribute));
		for (Message component : components()) result.append("\n\n").append(component.toString());
		return result.toString();
	}

	private String stringOf(Attribute attribute) {
		return attribute.name + ":" + (isMultiline(attribute.value) ? indent(attribute.value) : " " + attribute.value);
	}

	private static String indent(String text) {
		return "\n\t" + text.replaceAll("\\n", "\n\t");
	}

	private boolean isMultiline(String value) {
		return value != null && value.contains("\n");
	}

	public static Message load(String message) {
		return load(message.getBytes()).attach(attachmentsOf(message));
	}

	private static List<String> attachmentsOf(String message) {
		int index;
		List<String> result = new ArrayList<>();
		while ((index = message.indexOf('@')) > 0) {
			message = message.substring(index+1);
			index = message.contains("\n") ? message.indexOf('\n'): message.length();
			result.add(message.substring(0,index));
		}
		return result;
	}

	public static Message load(byte[] bytes) {
		try {
			return Loader.Inl.of(new ByteArrayInputStream(bytes)).next();
		} catch (IOException e) {
			return empty;
		}
	}

	private List<Message> components() {
		return components == null ? new ArrayList<Message>() : components;
	}

	private String path() {
		return owner != null ? owner.path() + "." + type : type;
	}

	public static Message empty = new Message("");

	public int length() {
		return toString().length();
	}

	public List<String> attributes() {
		return new ArrayList<>(attributes.keySet());
	}

	public Attachment attachment(String id) {
		if (id.startsWith("@")) id = id.substring(1);
		return attachments.containsKey(id) ? attachments.get(id.toLowerCase()) : null;
	}

	public boolean contains(String attribute) {
		return attributes.containsKey(attribute.toLowerCase());
	}

	public interface Value {
		<T> T as(Class<T> type);
	}
	static class Attribute {
		String name;
		String value;

		Attribute() {
		}

		Attribute(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name + ": " + value;
		}
	}

	public static class Attachment {
		private String id;
		private byte[] data;

		private Attachment(String id, byte[] data) {
			this.id = id;
			this.data = data;
		}

		public String id() {
			return id;
		}

		public String type() {
			return id.substring(id.lastIndexOf('.')+1);
		}

		public byte[] data() {
			return data;
		}

		public void data(byte[] data) {
			this.data = data;
		}
	}

}

