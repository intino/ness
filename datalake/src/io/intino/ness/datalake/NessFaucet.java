package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.IOException;
import java.util.Iterator;

import static io.intino.ness.datalake.NessDataLake.Channel;
import static io.intino.ness.datalake.NessDataLake.Reservoir;

public class NessFaucet {
    private final Channel channel;
    private final Iterator<Reservoir> reservoirs;
    private MessageInputStream inputStream;

    public NessFaucet(Channel channel) {
        this.channel = channel;
        this.reservoirs = channel.reservoirs().iterator();
        this.inputStream = nextInputStream();
    }

    public Channel channel() {
        return channel;
    }

    public String name() {
        return channel.name();
    }


    public Message next() throws IOException {
        while (inputStream != null) {
            Message message = inputStream.next();
            if (message != null) return message;
            inputStream = nextInputStream();
        }
        return null;
    }

    private MessageInputStream nextInputStream() {
        try {
            if (!reservoirs.hasNext()) return null;
            return reservoirs.next().input();
        }
        catch (Exception e) {
            e.printStackTrace();
            return new MessageInputStream.Empty();
        }
    }


}
