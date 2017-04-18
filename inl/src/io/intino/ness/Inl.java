package io.intino.ness;

import io.intino.ness.inl.*;
import io.intino.ness.inl.Accessory.Formatter;
import io.intino.ness.inl.Accessory.Parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.inl.Accessory.formatters;
import static io.intino.ness.inl.Accessory.parsers;

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

    static {
        String version = System.getProperty("java.version");
        Class<?> instantClass = instantClass();
        if (version.startsWith("1.8") && instantClass != null) {
            formatters.put(instantClass, instantFormatter());
            parsers.put(instantClass, instantParserOf(instantClass));
        }
    }

    private static Class<?> instantClass() {
        try {
            return Class.forName("java.time.Instant");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Parser instantParserOf(Class<?> instantClass)  {
        try {
            final Method method = instantClass.getDeclaredMethod("parse", CharSequence.class);
            return new Parser() {
                @Override
                public Object parse(String text)  {
                    try {
                        return method.invoke(null, text);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Formatter instantFormatter() {
        return new Formatter() {
            @Override
            public String format(Object value) {
                return value.toString();
            }
        };
    }

}
