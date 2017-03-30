package io.intino.ness.datalake;

import java.util.List;

public interface NessAction extends Runnable {
    void kill();

    interface Provider {
        List<NessFunctionContainer.Plug> plugsFor(String topic);
        NessMessageFaucet open(String topic);
        NessMessageFlooder flood();
    }


}
