package io.intino.ness.inl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static io.intino.ness.inl.Accessory.*;

public interface MessageInputStream {
    Message next();

    class Inl implements MessageInputStream {
        private BufferedReader reader;
        private Message message;

        public Inl(InputStream is) {
            this.reader = new BufferedReader(new InputStreamReader(is), 65536);
            this.message = createMessage(typeIn(nextLine()), null);
        }

        @Override
        public Message next() {
            if (message == null) return null;
            Attribute attribute = new Attribute();
            Message scope = message;
            while (true) {
                String line = nextLine();
                if (line == null) return swap(null);
                else if (isMultilineIn(line)) scope.write(attribute.add(line.substring(1)));
                else if (isAttributeIn(line)) scope.write(attribute = attribute.parse(line));
                else if (isMessageIn(line)) {
                    Message owner = ownerIn(line);
                    Message message = createMessage(typeIn(line), owner);
                    if (owner == null) return swap(message);
                    else scope = message;
                }
            }
        }

        private Message swap(Message message) {
            Message result = this.message;
            this.message = message;
            return result;
        }

        private String nextLine() {
            try {
                return normalize(reader.readLine());
            } catch (IOException e) {
                return null;
            }
        }

        private Message createMessage(String type, Message owner) {
            Message message = new Message(type, owner);
            if (owner != null) owner.components.add(message);
            return message;
        }

        private Message ownerIn(String line) {
            if (!line.contains(".")) return null;
            Message result = message;
            for (int i = 1; i < pathOf(line).length - 1; i++)
                result = lastItemOf(result.components);
            return result;
        }

        private Message lastItemOf(List<Message> messages) {
            return messages.isEmpty() ? null : messages.get(messages.size() - 1);
        }

        private static String typeIn(String line) {
            String[] path = pathOf(line);
            return path[path.length - 1];
        }

        private static String[] pathOf(String line) {
            line = line.substring(1, line.length() - 1);
            return line.contains(".") ? line.split("\\.") : new String[]{line};
        }
    }

    class Csv implements MessageInputStream {
        private final String type;
        private final BufferedReader reader;
        private final String[] header;

        public Csv(String type, InputStream is) {
            this.type = type;
            this.reader = new BufferedReader(new InputStreamReader(is), 65536);
            this.header = nextRow();
        }

        private String[] nextRow() {
            try {
                String line = reader.readLine();
                return line != null ? line.split(";") : new String[0];
            } catch (IOException e) {
                return new String[0];
            }
        }

        @Override
        public Message next() {
            String[] data = nextRow();
            if (data.length == 0) return null;
            Message message = new Message(type);
            for (int i = 0; i < Math.min(data.length, header.length); i++)
                message.write(header[i], data[i]);
            return message;
        }
    }

    class Empty implements MessageInputStream {
        @Override
        public Message next() {
            return null;
        }
    }
}
