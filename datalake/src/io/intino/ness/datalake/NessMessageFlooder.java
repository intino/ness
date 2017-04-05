package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.datalake.NessDataLake.Topic;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.time.Instant;

public class NessMessageFlooder {
    private final Writer writer;

    public NessMessageFlooder(Topic topic) {
        this.writer = new Writer(topic);
    }

    public void add(Message message) {
        if (message == null) return;
        writer.write(message);
    }

    public void close() {
        writer.close();
    }

    public static class Writer {
        private Topic topic;
        private Reservoir currentReservoir = null;

        public Writer(Topic topic) {
            this.topic = topic;
        }

        public void write(Message message) {
            try {
                open(reservoirOf(message));
                currentReservoir.outputStream().write(message);
            }
            catch (IOException ignored) {
            }
        }

        private void open(Reservoir reservoir) throws IOException {
            if (reservoir.equals(currentReservoir)) return;
            close();
            currentReservoir = reservoir;
        }

        public void close() {
            if (currentReservoir == null) return;
            try {
                currentReservoir.outputStream().close();
            } catch (IOException ignored) {
            }
            currentReservoir = null;
        }

        private Reservoir reservoirOf(Message message) {
            return topic.create(tsOf(message));
        }

        private Instant tsOf(Message message) {
            return message.read("ts").as(Instant.class);
        }


    }

}
