package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Topic;
import io.intino.ness.datalake.actions.PumpAction;
import io.intino.ness.datalake.virtual.VirtualTopic;
import io.intino.ness.inl.Inl;

import java.util.*;

public class NessFunctionContainer {
    private final NessDataLake dataLake;
    private final Map<Thread, NessAction> actions = new HashMap<>();

    public NessFunctionContainer(NessDataLake dataLake) {
        this.dataLake = dataLake;
    }

    public Pumping pump(String topic) {
        return new Pumping(topic);
    }

    public class Pumping {
        private Topic topic;

        public Pumping(String topic) {
            this.topic = dataLake.get(topic);
        }

        public Pumping with(String function) {
            return with(classOf(function));
        }

        public Pumping with(String function, String... sources) {
            return with(compile(function, sources));
        }

        public Pumping with(Class<? extends NessFunction> nessFunctionClass) {
            try {
                return with(nessFunctionClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }

        private Pumping with(NessFunction function) {
            topic = new VirtualTopic(topic, function);
            return this;
        }

        public Thread into(String target) {
            return start(createPumpAction(target));
        }

        private NessAction createPumpAction(String target) {
            return new PumpAction(faucet(), flooderOf(target));
        }

        private NessMessageFlooder flooderOf(String target) {
            return new NessMessageFlooder(dataLake.get(target));
        }

        private NessMessageFaucet faucet() {
            return new NessMessageFaucet(this.topic);
        }
    }

    public void kill(Thread thread) {
        if (!actions.containsKey(thread)) return;
        actions.get(thread).kill();
    }

    public Set<Thread> threads() {
        return actions.keySet();
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


    static  {
        Inl.init();
    }

}
