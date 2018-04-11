package io.intino.ness.datalake;

import io.intino.ness.inl.MessageInputStream;

import java.time.Instant;

public interface Tank {
    String name();

    Tub[] tubs();
    Tub get(Instant instant);

    interface Tub {
        String name();
        MessageInputStream input();
    }

}
