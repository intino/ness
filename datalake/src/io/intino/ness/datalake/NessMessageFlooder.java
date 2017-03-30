package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.datalake.NessDataLake.Topic;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NessMessageFlooder {
    private final NessDataLake dataLake;
    private final Map<String, Writer> writers = new HashMap<>();

    public NessMessageFlooder(NessDataLake dataLake) {
        this.dataLake = dataLake;
    }

    public void add(Message message) {
        if (message == null || message.topic() == null || message.topic().isEmpty()) return;
        writerOf(message.topic()).write(message);
    }

    private Writer writerOf(String topic) {
        if (!writers.containsKey(topic)) writers.put(topic, new Writer(get(topic)));
        return writers.get(topic);
    }

    public void close() {
        writers.values().forEach(Writer::close);
    }

    private Topic get(String topic) {
        return dataLake.get(topic);
    }

    public static class Writer {
        private Topic topic;
        private Reservoir currentReservoir = null;

        public Writer(Topic topic) {
            this.topic = topic;
        }

        public void write(Message message) {
            String data = message.toString() + "\n\n";
            try {
                open(reservoirOf(message));
                currentReservoir.outputStream().write(data.getBytes());
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
            currentReservoir.close();
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
