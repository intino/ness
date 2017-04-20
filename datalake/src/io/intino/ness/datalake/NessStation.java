package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;

import java.util.ArrayList;
import java.util.List;

public interface NessStation {

    List<String> channels();
    boolean exists(String channel);

    void create(String channel);
    void remove(String channel);
    void rename(String channel, String newName);
    void settle(String channel, Joint joint);

    Feed feed(String channel);
    Pipe pipe(String channel);
    Flow flow(String channel);

    void remove(Feed... feeds);
    void remove(Pipe... pipes);
    void remove(Flow... flows);

    List<Feed> feedsTo(String channel);
    List<Pipe> pipesFrom(String channel);
    List<Pipe> pipesTo(String channel);
    List<Pipe> pipesBetween(String source, String target);
    List<Flow> flowsFrom(String channel);

    Pump pump(String channel);
    Task seal(String channel);


    interface Feed extends Post {
    }

    interface Flow {
        Flow onMessage(Post post);
        default void send(Message message) {
            throw new RuntimeException("");
        }
    }

    interface Pipe extends MessageMapper {
        String from();
        String to();

        Pipe with(Valve valve);
        Pipe to(String channel);
    }

    interface Pump {
        Pump to(String channel);
        Pump to(Post post);
        Task start();
    }

    abstract class Task {
        private Thread thread;
        private boolean running = true;
        private List<Runnable> onTerminate = new ArrayList<>();

        public Task() {
            this.thread = new Thread(runnable());
            this.thread.start();
        }

        private Runnable runnable() {
            return new Runnable() {
                @Override
                public void run() {
                    if (init()) {
                        while (running)
                            running = step();
                        terminate();
                    }
                }
            };
        }

        public Thread thread() {
            return thread;
        }

        protected abstract boolean init();

        protected abstract boolean step();

        private void terminate() {
            onTerminate();
            if (onTerminate != null) onTerminate.forEach(Runnable::run);
        }

        protected void onTerminate() {

        }

        public void stop() {
            running = false;
        }

        private void onTerminate(Runnable runnable) {
            this.onTerminate.add(runnable);
        }

    }

}
