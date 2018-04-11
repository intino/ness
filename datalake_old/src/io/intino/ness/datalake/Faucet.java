package io.intino.ness.datalake;

import io.intino.ness.inl.Message;

import java.io.IOException;


public interface Faucet {
    Message next() throws IOException;

}
