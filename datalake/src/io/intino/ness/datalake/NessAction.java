package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Topic;

import java.util.List;

public interface NessAction extends Runnable {
    void kill();

    interface Provider {
        NessMessageFaucet open(Topic topic);
        NessMessageFlooder flood();
    }


}
