package io.intino.ness;

import io.intino.ness.inl.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

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
        return Deserializer.deserialize(new ByteArrayInputStream(text.getBytes())).next(aClass);
    }

    public static <T> List<T> deserializeAsList(String text, Class<T> tClass) {
        ArrayList<T> list = new ArrayList<>();
        final Deserializer deserialize = Deserializer.deserialize(new ByteArrayInputStream(text.getBytes(UTF_8)));
        T object;
        while ((object = deserialize.next(tClass)) != null)
            list.add(object);
        return list;
    }

    public static Message load(String text) {
        return loadAll(text).get(0);
    }

    public static List<Message> loadList(String text) {
        List<Message> list = new ArrayList<>();
        try {
            MessageInputStream inputStream = Formats.Inl.of(new ByteArrayInputStream(text.getBytes()));
            while (true) {
                Message message = inputStream.next();
                if (message == null) break;
                list.add(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}
