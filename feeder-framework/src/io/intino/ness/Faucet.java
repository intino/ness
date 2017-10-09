package io.intino.ness;

import io.intino.ness.inl.Message;

import java.io.IOException;


public interface Faucet {
    Message next() throws IOException;

}
