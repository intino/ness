package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFilter;
import io.intino.ness.inl.MessageMapper;

import java.util.ArrayList;
import java.util.List;

public class Valve implements MessageMapper {
    List<MessageMapper> mappers = new ArrayList<>();

    public static Valve define() {
        return new Valve();
    }

    @Override
    public Message map(Message message) {
        for (MessageMapper mapper : mappers) message = mapper.map(message);
        return message;
    }

    public Valve map(String function) throws Exception {
        return map(mapperClassOf(function));
    }

    public Valve map(String function, String... sources) throws Exception {
        return map(compile(function, sources).as(MessageMapper.class));
    }

    public Valve map(Class<? extends MessageMapper> mapperClass) throws Exception {
        return map(mapperClass.newInstance());
    }

    public Valve map(MessageMapper mapper) throws Exception {
        this.mappers.add(mapper);
        return this;
    }

    public Valve filter(String function) throws Exception {
        return filter(filterClassOf(function));
    }

    public Valve filter(String function, String... sources) throws Exception {
        return filter(compile(function, sources).as(MessageFilter.class));
    }

    public Valve filter(Class<? extends MessageFilter> filterClass) throws Exception {
        return filter(filterClass.newInstance());
    }

    public Valve filter(MessageFilter filter) throws Exception {
        return map(input -> filter.filter(input) ? input : null);
    }

    @SuppressWarnings("unchecked")
    private Class<MessageMapper> mapperClassOf(String name) throws ClassNotFoundException {
        return (Class<MessageMapper>) Class.forName(name);
    }

    @SuppressWarnings("unchecked")
    private Class<MessageFilter> filterClassOf(String name) throws ClassNotFoundException {
        return (Class<MessageFilter>) Class.forName(name);
    }

    private NessCompiler.Result compile(String function, String... sources) {
        return NessCompiler.compile(sources)
                .with("-target", "1.8")
                .load(function);
    }


    @Override
    public String toString() {
        return "[" + mappers.size() + "]";
    }
}
