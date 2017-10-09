package io.intino.ness.inl;

import io.intino.ness.inl.Formats.Inl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.inl.Deserializer.deserialize;

public class Message {
    String type;
    Message owner;
    List<Attribute> attributes;
    List<Message> components;

    public Message(String type) {
        this.type = type;
        this.owner = null;
        this.attributes = new ArrayList<>();
        this.components = null;
    }

    Message(String type, Message owner) {
        this.type = type;
        this.owner = owner;
        this.attributes = new ArrayList<>();
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
        return read("ts");
    }

    public void ts(String ts) {
        write("ts", ts);
    }

    public String read(String attribute) {
        return valueOf(attribute);
    }

    public Message write(String attribute, String value) {
        if (contains(attribute))
            get(attribute).value = value;
        else if (value != null)
            attributes.add(new Attribute(attribute,value));
        return this;
    }

    public Data parse(final String attribute) {
        return new Data() {
            @Override @SuppressWarnings("unchecked")
            public <T> T as(Class<T> type) {
                String value = valueOf(attribute);
                return value != null ? (T) Accessory.parsers.get(type).parse(deIndent(value)) : null;
            }
        };
    }

    private String deIndent(String value) {
        return value.startsWith("\n") ? value.substring(1) : value;
    }

    Message write(Attribute attribute) {
        return write(attribute.name, attribute.value);
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

    public Message rename(String attribute, String newName) {
        get(attribute).name = newName;
        return this;
    }

    public Message remove(String attribute) {
        if (contains(attribute)) attributes.remove(indexOf(attribute));
        return this;
    }

    private String valueOf(String attribute) {
        return get(attribute).value;
    }

    public int indexOf(String attribute) {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.get(i).name.equalsIgnoreCase(attribute)) return i;
        return -1;
    }

    public boolean contains(String attribute) {
        return indexOf(attribute) >= 0;
    }

    private Attribute get(String attribute) {
        int indexOf = indexOf(attribute);
        return indexOf >= 0 ? attributes.get(indexOf) : new Attribute();
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
        String result = "[" + path() + "]" ;
        for (Attribute attribute : attributes) result += "\n" + attribute.toString();
        for (Message component : components()) result += "\n\n" + component.toString();
        return result;
    }

    public static Message load(String message) {
        return load(message.getBytes());
    }

    public static Message load(byte[] bytes) {
        try {
            return Inl.of(new ByteArrayInputStream(bytes)).next();
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
        List<String> result = new ArrayList<>();
        for (Attribute attribute : attributes) result.add(attribute.name);
        return result;
    }

    public interface Data {
        <T> T as(Class<T> type);
    }

    static class Attribute {
        String name;
        String value;

        Attribute() {

        }

        Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        Attribute parse(String line) {
            int i = line.indexOf(":");
            name = line.substring(0, i);
            value = i < line.length() - 1 ? unwrap(line.substring(i + 1)) : "";
            return this;
        }

        Attribute add(String line) {
            value = value + "\n" + line;
            return this;
        }

        private String unwrap(String text) {
            return text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1) : text;
        }

        @Override
        public String toString() {
            return name + ": " + (isMultiline() ? indent(value) : value);
        }

        private static String indent(String text) {
            return text.replaceAll("\\n", "\n\t");
        }

        private boolean isMultiline() {
            return value != null && value.contains("\n");
        }
    }
}

