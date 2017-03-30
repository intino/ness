package io.intino.ness.datalake;

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
        Reservoir get(Instant ts);
    }

    interface Reservoir {
        String name();
        Format format();

        InputStream inputStream();
        OutputStream outputStream();

        void close();
    }

    enum Format {
        inl, csv, unknown
    }
}
