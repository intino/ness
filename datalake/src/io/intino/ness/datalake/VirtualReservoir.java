package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageOutputStream;

import static java.util.Arrays.stream;

public class VirtualReservoir implements Reservoir {
    private final Reservoir reservoir;
    private final NessFunction function;

    public VirtualReservoir(Reservoir reservoir, NessFunction function) {
        this.reservoir = reservoir;
        this.function = function;
    }

    @Override
    public String name() {
        return reservoir.name();
    }

    @Override
    public MessageInputStream[] inputs() {
        return stream(reservoir.inputs())
                .map(this::create)
                .toArray(MessageInputStream[]::new);
    }

    private MessageInputStream create(MessageInputStream mis) {
        return () -> cast(mis.next());
    }

    private Message cast(Message message) {
        return message != null ? function.cast(message) : null;
    }

    @Override
    public MessageOutputStream output() {
        return null;
    }

    @Override
    public MessageOutputStream feed() {
        return null;
    }

}
