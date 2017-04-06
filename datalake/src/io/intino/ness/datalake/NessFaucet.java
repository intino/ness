package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.util.Iterator;

import static io.intino.ness.datalake.NessDataLake.Reservoir;
import static io.intino.ness.datalake.NessDataLake.Topic;
import static java.util.Arrays.stream;

public class NessFaucet {
    private final Topic topic;
    private final Iterator<Reservoir> reservoirs;
    private MessageInputStream inputStream;

    public NessFaucet(Topic topic) {
        this.topic = topic;
        this.reservoirs = topic.reservoirs().iterator();
        this.inputStream = nextInputStream();
    }

    public Topic topic() {
        return topic;
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
        return collect(reservoirs.next().inputs());
    }

    private MessageInputStream collect(MessageInputStream[] inputStreams) {
        if (inputStreams.length == 0) return emptyMessageInputStream();
        if (inputStreams.length == 1) return inputStreams[0];
        return new MessageInputStream() {
            Message[] messages = stream(inputStreams)
                    .map(MessageInputStream::next)
                    .toArray(Message[]::new);

            @Override
            public Message next() {
                int index = indexOfMinTimestamp();
                if (index == -1) return null;
                Message message = messages[index];
                messages[index] = inputStreams[index].next();
                return message;
            }

            private int indexOfMinTimestamp() {
                int index = -1;
                String min = "99999999";
                String ts;
                for (int i = 0; i < messages.length; i++) {
                    if ((ts = get(i)) == null) continue;
                    if (ts.compareTo(min) >= 0) continue;
                    index = i;
                    min = ts;
                }
                return index;
            }

            private String get(int index) {
                return get(messages[index]);
            }

            private String get(Message message) {
                if (message == null) return null;
                if (!message.contains("ts")) throw new Exception("Legacy topics with multiple datafiles require a timestamp generator function");
                return message.read("ts").as(String.class);
            }
        };
    }

    private MessageInputStream emptyMessageInputStream() {
        return () -> null;
    }


    public static class Exception extends RuntimeException {

        public Exception(String message) {
            super(message);
        }
    }


}
