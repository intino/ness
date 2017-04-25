package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;

import java.util.List;

public interface NessStation {

    Tank tank(String tank);
    Feed feed(String tank);
    Pipe pipe(String tank);
    Flow flow(String tank);

    void remove(String tank);
    void remove(Feed... feeds);
    void remove(Pipe... pipes);
    void remove(Flow... flows);

    List<Tank> tanks();
    List<Feed> feedsTo(String tank);
    List<Flow> flowsFrom(String tank);
    List<Pipe> pipesFrom(String tank);
    List<Pipe> pipesTo(String tank);
    Pipe pipeBetween(String source, String target);

    Pump pump(String tank);
    Job seal(String tank);

    boolean exists(String tank);
    void rename(String tank, String newName);

    interface Feed {
        void send(Message message);
        default void flush() {}
    }

    interface Flow {
        Flow to(Post post);
        default void send(Message message) {
            throw new RuntimeException("");
        }
    }

    interface Pipe extends MessageMapper {
        String from();
        String to();

        Pipe with(Valve valve);
        Pipe to(String tank);
    }

    interface Pump {
        Pump to(String tank);
        Pump to(Post post);
        Job start();
    }

}
