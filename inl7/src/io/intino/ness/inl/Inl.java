package io.intino.ness.inl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Inl {

    public static String serialize(Object object) {
        return Serializer.serialize(object).toInl();
    }

    public static <T> T deserialize(InputStream is, Class<T> aClass) {
        return Deserializer.deserialize(is).next(aClass);
    }

    public static <T> T deserialize(String text, Class<T> aClass) {
        final Deserializer deserialize = Deserializer.deserialize(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        return deserialize.next(aClass);
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
