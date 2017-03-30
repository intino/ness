package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.util.Iterator;

import static io.intino.ness.datalake.NessDataLake.*;
import static io.intino.ness.datalake.NessDataLake.Format.*;

public class NessMessageFaucet {
    private final Topic topic;
    private final Iterator<Reservoir> reservoirs;
    private MessageInputStream inputStream;

    public NessMessageFaucet(Topic topic) {
        this.topic = topic;
        this.reservoirs = topic.reservoirs().iterator();
        this.inputStream = nextInputStream();
    }

    public Message next() {
        while (inputStream != null) {
            Message message = inputStream.next();
            if (message != null) return message;
            inputStream = nextInputStream();
        }
        return null;
    }

    private MessageInputStream nextInputStream() {
        if (!reservoirs.hasNext()) return null;
        return messageInputStreamOf(reservoirs.next());
    }

    public MessageInputStream messageInputStreamOf(Reservoir reservoir) {
        if (reservoir.format() == inl)
            return new MessageInputStream.Inl(reservoir.inputStream());
        if (reservoir.format() == csv)
            return new MessageInputStream.Csv(typeOf(topic), reservoir.inputStream());
        return new MessageInputStream.Empty();
    }

    private String typeOf(Topic topic) {
        String name = topic.name();
        return name.substring(name.lastIndexOf(".")+1);
    }


}
