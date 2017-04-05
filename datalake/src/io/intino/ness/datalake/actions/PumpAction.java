package io.intino.ness.datalake.actions;

import io.intino.ness.datalake.*;
import io.intino.ness.inl.Message;

public class PumpAction implements NessAction {
    private final NessMessageFaucet faucet;
    private final NessMessageFlooder flooder;
    private volatile boolean running;

    public PumpAction(NessMessageFaucet faucet, NessMessageFlooder flooder) {
        this.faucet = faucet;
        this.flooder = flooder;
    }

    @Override
    public void run() {
        this.running = true;
        while (running) {
            Message message = faucet.next();
            if (message == null) break;
            flooder.add(message);
        }
        flooder.close();
    }

    @Override
    public void kill() {
        this.running = false;
    }

}
