package io.intino.ness.inl;

import io.intino.ness.inl.streams.MessageInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Inl {

    static {
        init();
    }

    public static void init() {
        Accessory.formatters.put(Instant.class, Object::toString);
        Accessory.parsers.put(Instant.class, Instant::parse);
    }

    public static String serialize(Object object) {
        return Serializer.serialize(object).toInl();
    }

    public static <T> T deserialize(InputStream is, Class<T> aClass) {
        return Deserializer.deserialize(is).next(aClass);
    }

    public static <T> T deserialize(String text, Class<T> aClass) {
        return Deserializer.deserialize(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))).next(aClass);
    }

    public static Message messageOf(String text) {
        return new MessageInputStream.Inl(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))).next();
    }

    public static <T> List<T> deserializeAsList(String text, Class<T> tClass) {
        ArrayList<T> list = new ArrayList<>();
        final Deserializer deserialize = Deserializer.deserialize(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        T object;
        while ((object = deserialize.next(tClass)) != null)
            list.add(object);
        return list;
    }

}
