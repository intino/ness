package io.intino.ness.datalake;

import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.List;

public interface NessDataLake {
    List<Topic> topics();
    Topic get(String topic);
    Manager manage();

    interface Manager {
        void create(String topic);
        void remove(String topic);
        void rename(String topic, String newName);
    }

    interface Topic {
        String name();
        List<Reservoir> reservoirs();
        Reservoir get(Instant instant);
    }

    interface Reservoir {
        String name();

        MessageInputStream[] inputs();
        MessageOutputStream output();
        MessageOutputStream feed();
    }

}
