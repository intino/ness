package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.datalake.NessDataLake.Topic;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.time.Instant;

public class NessFeeder {
    private final Topic topic;
    private final Writer writer;

    public NessFeeder(Topic topic) {
        this.topic = topic;
        this.writer = new Writer();
    }

    public void pump(Message message) throws IOException {
        if (message == null) return;
        writer.write(message);
    }

    public void feed(Message message) throws IOException {
        if (message == null) return;
        writer.write(message);
    }

    public void close() {
        writer.close();
    }

    public class Writer {
        private Reservoir currentReservoir = null;

        public void write(Message message) throws IOException {
            open(reservoirOf(message));
            currentReservoir.feed().write(message);
        }

        private void open(Reservoir reservoir) throws IOException {
            if (reservoir.equals(currentReservoir)) return;
            close();
            currentReservoir = reservoir;
        }

        public void close() {
            if (currentReservoir == null) return;
            try {
                currentReservoir.feed().close();
            } catch (IOException ignored) {
            }
            currentReservoir = null;
        }

        private Reservoir reservoirOf(Message message) {
            return topic.get(tsOf(message));
        }

        private Instant tsOf(Message message) {
            return message.read("ts").as(Instant.class);
        }


    }

}
