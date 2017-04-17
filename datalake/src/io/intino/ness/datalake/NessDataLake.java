package io.intino.ness.datalake;

import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageOutputStream;

import java.time.Instant;
import java.util.List;

public interface NessDataLake {
    List<Channel> channels();
    Channel get(String channel);

    interface Channel {
        String name();
        List<Reservoir> reservoirs();
        Reservoir get(Instant instant);
    }

    interface Reservoir {
        String name();
        MessageInputStream input();
    }

    interface Joint {
        MessageInputStream join(MessageInputStream[] inputStreams);
    }


}
