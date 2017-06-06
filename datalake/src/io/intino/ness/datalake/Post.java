package io.intino.ness.datalake;

import io.intino.ness.inl.Message;

public interface Post {
    void send(Message message);
    default void flush() {
    }
}
