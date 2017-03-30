package io.intino.ness.inl;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Deserializer {
    private final BufferedReader reader;
    private Accessory.Mapping mapping = new Accessory.Mapping();
    private String line;

    public static Deserializer deserialize(InputStream is) {
        return new Deserializer(is);
    }

    public static Deserializer deserialize(String text) {
        return deserialize(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    private Deserializer(InputStream is) {
        this.reader = new BufferedReader(new InputStreamReader(is));
        nextLine();
    }

    @SuppressWarnings("unchecked")
    public <T> T next(Class<T> type) {
        if (!startBlockOf(type)) return null;
        return fill((T) create(type));
    }

    private boolean startBlockOf(Class type) {
        return map(Accessory.unwrap(line)).equalsIgnoreCase(type.getSimpleName());
    }

    private String map(String id) {
        return mapping.get(id);
    }

    private <T> T fill(T object) {
        Object scope = object;
        Attribute attribute = new Attribute();
        nextLine();
        while (!isTerminated(object)) {
            if (Accessory.isMultilineIn(line)) setAttribute(scope, attribute.add(line.substring(1)));
            else
            if (Accessory.isMessageIn(line)) scope = addComponent(object, line.substring(1,line.length()-1));
            else
            if (Accessory.isAttributeIn(line)) setAttribute(scope, attribute.parse(line));
            nextLine();
        }
        return object;
    }

    private boolean isTerminated(Object object) {
        return line == null || startBlockOf(object.getClass());
    }

    private Object addComponent(Object scope, String path) {
        String[] paths = path.split("\\.");
        for (int i = 1; i < paths.length-1; i++) {
            scope = findScope(scope, paths[i]);
            if (scope == null) return null;
        }
        return createComponent(paths[paths.length-1], scope);
    }

    private void setAttribute(Object object, Attribute attribute) {
        if (object == null || attribute.value == null) return;
        Field field = Accessory.fieldsOf(object).get(map(attribute.name));
        setField(field, object, parserOf(field).parse(attribute.value));
    }

    private Object findScope(Object object, String attribute) {
        for (Field field : Accessory.fieldsOf(object).asList()) {
            if (!match(field, attribute)) continue;
            Object result = valueOf(field, object);
            return result instanceof List ? lastItemOf((List) result) : result;
        }
        return null;
    }

    private boolean match(Field field, String attribute) {
        return  attribute.equalsIgnoreCase(field.getName()) ||
                attribute.equalsIgnoreCase(classOf(field).getSimpleName());
    }

    private Object valueOf(Field field, Object object) {
        try {
            field.setAccessible(true);
            return field.get(object);
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    private Object lastItemOf(List list) {
        return list.get(list.size()-1);
    }

    private Class classOf(Field field) {
        if (!(field.getGenericType() instanceof ParameterizedType)) return field.getType();
        ParameterizedType ptype = (ParameterizedType) field.getGenericType();
        return (Class) ptype.getActualTypeArguments()[0];
    }

    private Object createComponent(String type, Object object) {
        return createComponent(findField(type, object), object);
    }

    private Object createComponent(Field field, Object object) {
        if (field == null) return null;
        if (isList(field)) return createListItem(field, object);
        return setField(field, object, create(classOf(field)));
    }

    @SuppressWarnings("unchecked")
    private Object createListItem(Field field, Object object) {
        List list = (List) valueOf(field, object);
        if (list == null) {
            list = new ArrayList<>();
            setField(field, object, list);
        }
        Object item = create(classOf(field));
        list.add(item);
        return item;
    }

    private boolean isList(Field field) {
        return field.getType().isAssignableFrom(List.class);
    }

    private Object setField(Field field, Object object, Object value) {
        if (field == null) return null;
        try {
            field.setAccessible(true);
            field.set(object, value);
            return value;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Field findField(String type, Object object) {
        for (Field field : Accessory.fieldsOf(object).asList()) {
            if (!match(field, type)) continue;
            if (isList(field) || valueOf(field, object) == null) return field;
        }
        return null;
    }

    private Object create(Class<?> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    private void nextLine()  {
        try {
            do {
                line = Accessory.normalize(reader.readLine());
            } while (line != null && line.isEmpty());
        }
        catch (IOException e) {
            line = null;
        }
    }

    private Accessory.Parser parserOf(Field field) {
        return Accessory.parsers.get(field.getType());
    }

    public Deserializer map(String from, String to) {
        mapping.put(from,to);
        return this;
    }
}
