package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFilter;
import io.intino.ness.inl.MessageFunction;
import io.intino.ness.inl.MessageMapper;

import java.util.ArrayList;
import java.util.List;

public interface NessPumpingStation {

    void create(String channel);
    void remove(String channel);
    void rename(String channel, String newName);
    boolean exists(String channel);

    Channeling pipe();
    Channeling pipe(String channel);

    boolean close(Pipe pipe);

    Task pump(String channel) throws Exception;
    Task seal(String channel);

    interface Channeling {
        Channeling join(Joint joint);
        Channeling map(String function) throws Exception;
        Channeling map(String function, String... sources) throws Exception;
        Channeling map(Class<? extends MessageMapper> mapperClass) throws Exception;
        Channeling map(MessageMapper mapper) throws Exception;
        Channeling filter(String function) throws Exception;
        Channeling filter(String function, String... sources) throws Exception;
        Channeling filter(Class<? extends MessageFilter> filterClass) throws Exception;
        Channeling filter(MessageFilter filter) throws Exception;
        Pipe to(String channel);
        Pipe to(Pipe pipe);
    }

    interface Pipe {
        void send(Message message);
    }

    interface SingleUsePipe extends Pipe {
        default void flush() {}
    }

    abstract class Task implements Runnable {
        private Thread thread;
        private boolean running = true;
        private List<Runnable> onTerminate = new ArrayList<>();

        public Task() {
            this.thread = new Thread(this);
            this.thread.start();
        }

        public Thread thread() {
            return thread;
        }

        public void run() {
            if (init()) {
                while (running)
                    running = step();
                terminate();
            }
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
