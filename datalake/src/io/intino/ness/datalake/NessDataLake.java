package io.intino.ness.datalake;

import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;

public interface NessDataLake {
    List<Topic> topics();
    Topic get(String topic);

    interface Topic {
        String name();
        List<Reservoir> reservoirs();

        Reservoir create(Instant instant);
    }

    interface Reservoir {
        String name();

        MessageInputStream[] inputStreams();
        MessageOutputStream outputStream();
    }

    enum Serie {
        single, multiple
    }

    enum Format {
        inl, csv, dat, xml, json, unknown
    }
}
