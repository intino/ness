package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Topic;
import io.intino.ness.inl.Inl;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class NessPump {
    private final NessDataLake dataLake;
    private final List<Plug> plugs = new ArrayList<>();

    public NessPump(NessDataLake dataLake) {
        this.dataLake = dataLake;
    }

    public Plug plug(String topic) {
        return new Plug(topic);
    }

    public Task execute() {
        Task task = new Task();
        task.execute();
        return task;
    }

    public class Plug {
        private Topic source;
        private NessFeeder feeder;

        Plug(String source) {
            this.source = dataLake.get(source);
        }

        public Plug with(String function) throws Exception {
            return with(classOf(function));
        }

        public Plug with(String function, String... sources) throws Exception {
            return with(compile(function, sources));
        }

        public Plug with(Class<? extends NessFunction> nessFunctionClass) throws Exception {
            return with(nessFunctionClass.newInstance());
        }

        public void into(String target) {
            this.feeder = new NessFeeder(dataLake.get(target));
            plugs.add(this);
        }

        private Plug with(NessFunction function) {
            source = new VirtualTopic(source, function);
            return this;
        }

        private Class<NessFunction> compile(String function, String... sources) {
            return NessCompiler.compile(sources).
                    with("-target", "1.8").
                    load(function).
                    as(NessFunction.class);
        }

        @SuppressWarnings("unchecked")
        private Class<NessFunction> classOf(String name) throws ClassNotFoundException {
            return (Class<NessFunction>) Class.forName(name);
        }

    }

    private List<NessFaucet> faucets() {
        return plugs.stream()
                .map(p->p.source)
                .distinct()
                .map(NessFaucet::new)
                .collect(toList());
    }

    private List<Plug> plugsOf(NessFaucet faucet) {
        return plugs.stream()
                .filter(p -> p.source == faucet.topic())
                .collect(toList());
    }

    public class Task implements Runnable {
        private boolean running = true;
        private Thread thread;

        private void execute() {
            thread = new Thread(this);
            thread.start();
        }

        public Thread thread() {
            return thread;
        }

        @Override
        public void run() {
            faucets().parallelStream().forEach(this::execute);
        }

        private void execute(NessFaucet faucet) {
            if (faucet.isEmpty()) throw new RuntimeException(faucet.name() + " is empty");
            List<Plug> plugs = plugsOf(faucet);
            while (running) {
                Message message = faucet.next();
                if (message == null) break;
                plugs.parallelStream()
                    .forEach(p -> pump(p, message));
            }
            plugs.forEach(p->p.feeder.close());

        }

        private void pump(Plug plug, Message message) {
            try {
                plug.feeder.pump(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void kill() {
            running = false;
        }

    }

    static  {
        Inl.init();
    }

}
