package io.intino.ness;

import io.intino.ness.inl.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Inl {

    public static String serialize(Object object) {
        return Serializer.serialize(object).toInl();
    }

    public static Deserializer deserialize(InputStream is) {
        return Deserializer.deserialize(is);
    }

    public static Deserializer deserialize(String text) {
        return deserialize(new ByteArrayInputStream(text.getBytes()));
    }

    public static <T> List<T> deserializeAll(InputStream is, Class<T> aClass) {
        List<T> list = new ArrayList<>();
        Deserializer deserialize = deserialize(is);
        while (true) {
            T object = deserialize.next(aClass);
            if (object == null) break;
            list.add(object);

        }
        return list;
    }

    public static <T> List<T> deserializeAll(String text, Class<T> aClass) {
        return deserializeAll(new ByteArrayInputStream(text.getBytes()), aClass);
    }

    public static List<Message> load(String text) {
        List<Message> list = new ArrayList<>();
        try {
            MessageInputStream inputStream = Loader.Inl.of(new ByteArrayInputStream(text.getBytes()));
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
