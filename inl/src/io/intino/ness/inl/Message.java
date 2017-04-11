package io.intino.ness.inl;

import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.inl.Deserializer.*;

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

    public String read(String attribute) {
        return valueOf(attribute);
    }

    public void write(String attribute, String value) {
        if (contains(attribute))
            get(attribute).value = value;
        else
        if (value != null) attributes.add(new Attribute(attribute,value));
    }

    public Data parse(final String attribute) {
        return new Data() {
            @Override @SuppressWarnings("unchecked")
            public <T> T as(Class<T> type) {
                String value = valueOf(attribute);
                return value != null ? (T) Accessory.parsers.get(type).parse(value) : null;
            }
        };
    }

    void write(Attribute attribute) {
        write(attribute.name, attribute.value);
    }

    public void write(String attribute, Boolean value) {
        get(attribute).value = value.toString();
    }

    public void write(String attribute, Integer value) {
        write(attribute, value.toString());
    }

    public void write(String attribute, Double value) {
        write(attribute, value.toString());
    }

    public void rename(String attribute, String newName) {
        get(attribute).name = newName;
    }

    public void remove(String attribute) {
        if (!contains(attribute)) return;
        attributes.remove(indexOf(attribute));
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
        for (Message component : components)
            if (component.is(type)) result.add(component);
        return result;
    }

    public void add(Message component) {
        if (components == null) components = new ArrayList<>();
        components.add(component);
        component.owner = this;
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

    private List<Message> components() {
        return components == null ? new ArrayList<Message>() : components;
    }

    private String path() {
        return owner != null ? owner.path() + "." + type : type;
    }

    private static Message empty = new Message("");
    public static Message empty() {
        return empty;
    }

    public interface Data {
        <T> T as(Class<T> type);
    }


}

