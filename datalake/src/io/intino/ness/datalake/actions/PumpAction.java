package io.intino.ness.datalake.actions;

import io.intino.ness.datalake.NessAction;
import io.intino.ness.datalake.NessFunctionContainer;
import io.intino.ness.datalake.NessMessageFlooder;
import io.intino.ness.datalake.NessMessageFaucet;
import io.intino.ness.inl.Message;

import java.util.List;

public class PumpAction implements NessAction {
    private final Provider provider;
    private final String topic;
    private volatile boolean running;

    public PumpAction(Provider provider, String topic) {
        this.provider = provider;
        this.topic = topic;
    }

    @Override
    public void run() {
        this.running = true;
        NessMessageFaucet faucet = provider.open(topic);
        NessMessageFlooder flooder = provider.flood();
        List<NessFunctionContainer.Plug> plugs = provider.plugsFor(topic);
        while (running) {
            Message message = faucet.next();
            if (message == null) break;
            plugs.forEach(p -> flooder.add(p.cast(message)));
        }
        flooder.close();
    }

    @Override
    public void kill() {
        this.running = false;
    }

    private void execute() {
        NessMessageFaucet reader = provider.open(topic);
        NessMessageFlooder flooder = provider.flood();
        List<NessFunctionContainer.Plug> plugs = provider.plugsFor(topic);
        while (running) {
            Message message = reader.next();
            if (message == null) break;
            plugs.forEach(p -> flooder.add(p.cast(message)));
        }
        flooder.close();
    }

}
