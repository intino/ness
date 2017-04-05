package io.intino.ness.inl;

import java.io.IOException;

public interface MessageOutputStream {

    void write(Message message) throws IOException;
    void close() throws IOException;
}
