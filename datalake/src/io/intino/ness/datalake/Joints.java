package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.IOException;

public class Joints {

    public static Joint sortingBy(String attribute) {
        return inputStreams -> {
            try {
                return new Sorter(inputStreams, attribute);
            } catch (IOException e) {
                e.printStackTrace();
                return new MessageInputStream.Empty();
            }
        };
    }


    private static class Sorter implements MessageInputStream {
        private MessageInputStream[] inputStreams;
        private Message[] messages;
        private String attribute;

        Sorter(MessageInputStream[] inputStreams, String attribute) throws IOException {
            this.inputStreams = inputStreams;
            this.messages = new Message[inputStreams.length];
            this.attribute = attribute;
            for (int i = 0; i < messages.length; i++)
                this.messages[i] = inputStreams[i].next();
        }

        @Override
        public String name() {
            return "sort join";
        }

        @Override
        public void name(String value) {

        }

        @Override
        public Message next() throws IOException {
            int index = indexOfNext();
            if (index == -1) return null;
            Message message = messages[index];
            messages[index] = inputStreams[index].next();
            return message;
        }

        @Override
        public void close() throws IOException {
            for (MessageInputStream inputStream : inputStreams)
                inputStream.close();
        }

        private int indexOfNext() {
            int index = -1;
            String min = "99999999";
            String val;
            for (int i = 0; i < messages.length; i++) {
                if (messages[i] == null) continue;
                val = messages[i].read(attribute);
                if (val.compareTo(min) >= 0) continue;
                index = i;
                min = val;
            }
            return index;
        }

    }
}
