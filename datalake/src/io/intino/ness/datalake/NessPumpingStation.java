package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFunction;

public interface NessPumpingStation {

    void create(String channel);
    void remove(String channel);
    void rename(String channel, String newName);
    boolean exists(String channel);

    Pipe feed(String channel);
    Channeling pipe(String channel);
    boolean close(Pipe pipe);


    Task pump(String channel) throws Exception;
    Task seal(String channel);

    interface Channeling {
        Channeling with(String function) throws Exception;
        Channeling with(String function, String... sources) throws Exception;
        Channeling with(Class<? extends MessageFunction> functionClass) throws Exception;
        Channeling with(MessageFunction function) throws Exception;
        Pipe to(String channel);
        Pipe to(Pipe... pipes);
        void join(Joint joint);
    }

    interface Pipe {
        void send(Message message);
        default void flush() {}
    }

    interface Task extends Runnable {
        void stop();
        Thread thread();

    }

}
