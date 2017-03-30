package io.intino.ness.datalake;

import io.intino.ness.datalake.NessAction.Provider;
import io.intino.ness.datalake.actions.PumpAction;
import io.intino.ness.inl.Inl;
import io.intino.ness.inl.Message;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class NessFunctionContainer {
    private final NessDataLake dataLake;
    private final Map<String, Plug> plugs = new HashMap<>();
    private final Map<Thread, NessAction> actions = new HashMap<>();

    public NessFunctionContainer(NessDataLake dataLake) {
        this.dataLake = dataLake;
    }

    public String plugs() {
        String result = "";
        for (Plug plug : plugs.values())
            result += plug.toString() + "\n";
        return result;
    }

    public Plugging plug(String name) {
        return topic -> plug(classOf(name), topic);
    }

    public Plugging plug(String name, String... sources)  {
        return topic -> plug(compile(name, sources), topic);
    }

    public Plugging plug(Class<? extends NessFunction> functionClass) {
        return topic -> plug(functionClass, topic);
    }

    public String unplug(String id) {
        if (!plugs.containsKey(id)) return "Function doesn't exist";
        plugs.remove(id);
        return "Function unplugged";
    }

    public List<Plug> plugsFor(String topic) {
        return plugs.values().stream().
                filter(p->p.inTopic.equalsIgnoreCase(topic)).
                collect(toList());
    }

    public Thread pump(String topic) {
        return start(new PumpAction(provider(), topic));
    }

    public void kill(Thread thread) {
        if (!actions.containsKey(thread)) return;
        actions.get(thread).kill();
    }

    public Set<Thread> threads() {
        return actions.keySet();
    }

    private String plug(Class<? extends NessFunction> functionClass, String inTopic)  {
        if (functionClass == null) return "Function not found";
        try {
            add(new Plug(functionClass.newInstance(), inTopic));
        } catch (InstantiationException | IllegalAccessException ignored) {
        }
        return "Function plugged to " + inTopic;
    }

    private void add(Plug plug) {
        plugs.put(UUID.randomUUID().toString(), plug);
    }

    private Thread start(NessAction action) {
        Thread thread = new Thread(action);
        thread.start();
        actions.put(thread, action);
        return thread;
    }

    private Class<NessFunction> compile(String function, String... sources) {
        return NessCompiler.compile(sources).
                with("-target", "1.8").
                load(function).
                as(NessFunction.class);
    }

    @SuppressWarnings("unchecked")
    private Class<NessFunction> classOf(String name) {
        try {
            return (Class<NessFunction>) Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    private Provider provider() {
        return new Provider() {
            @Override
            public List<Plug> plugsFor(String topic) {
                return NessFunctionContainer.this.plugsFor(topic);
            }

            @Override
            public NessMessageFaucet open(String topic) {
                return new NessMessageFaucet(dataLake.get(topic));
            }

            @Override
            public NessMessageFlooder flood() {
                return new NessMessageFlooder(dataLake);
            }
        };
    }

    public static class Plug {
        private NessFunction function;
        private String inTopic;
        private Instant executionInstant;
        private int executionCount;

        Plug(NessFunction function, String inTopic) {
            this.function = function;
            this.inTopic = inTopic;
        }

        public Message cast(Message input) {
            return use().function.cast(input);
        }

        private Plug use() {
            executionInstant = Instant.now();
            executionCount++;
            return this;
        }

        @Override
        public String toString() {
            return inTopic + " > " + function.getClass().getName() + " : " + execution();
        }

        private String execution() {
            if (executionInstant == null) return "Never executed";
            return String.format("%,d times execution", executionCount) + ". Last " + executionInstant.toString();
        }

    }

    public interface Plugging {
        String to(String topic);
    }

    static  {
        Inl.init();
    }

}
