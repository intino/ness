package io.intino.ness.inl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static io.intino.ness.inl.Accessory.*;

public class Formats {
    public static class Inl implements MessageInputStream {
        private String name;
        private BufferedReader reader;
        private Message message;

        public static MessageInputStream of(InputStream is) throws IOException {
            return new Inl(is);
        }

        private Inl(InputStream is) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(is), 65536);
            this.message = createMessage(typeIn(nextLine()), null);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void name(String value) {
            this.name = value;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Message next() throws IOException {
            if (message == null) return null;
            Message.Attribute attribute = new Message.Attribute();
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

        @Override
        public void close() throws IOException {
            reader.close();
        }

        private Message swap(Message message) {
            Message result = this.message;
            this.message = message;
            return result;
        }

        private String nextLine() throws IOException {
            return normalize(reader.readLine());
        }

        private Message createMessage(String type, Message owner) {
            Message message = new Message(type, owner);
            if (owner != null) owner.components.add(message);
            return message;
        }

        private Message ownerIn(String line) {
            if (!line.contains(".")) return null;
            Message result = message;
            for (int i = 1; i < pathOf(line).length - 1; i++) {
                assert result != null;
                result = lastItemOf(result.components);
            }
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

    public static class Tsv extends Csv {

        public static MessageInputStream of(InputStream is) throws IOException {
            return new Tsv(is);
        }

        private Tsv(InputStream is) throws IOException {
            super(is);
        }

        protected String[] nextRow() throws IOException {
            String line = reader.readLine();
            return line != null ? line.split("\\t") : null;
        }

    }

    public static class Dat extends Csv {

        protected final String[] data;

        public static MessageInputStream of(InputStream is) throws IOException {
            return new Dat(is);
        }

        private Dat(InputStream is) throws IOException {
            super(is);
            this.data = parse(headers);
            this.headers = nextRow();
        }

        private String[] parse(String[] headers) {
            List<String> data = new ArrayList<>();
            for (String header : headers) {
                data.addAll(parse(header.trim()));
            }
            return data.toArray(new String[data.size()]);
        }

        private List<String> parse(String header) {
            List<String> data = new ArrayList<>();
            for (String str : header.split(" ")) {
                int index = str.indexOf('=');
                if (index < 0) continue;
                data.add(str.substring(0,index));
                data.add(str.substring(index+1));
            }
            return data;
        }

        @Override
        public Message next() throws IOException {
            Message message = super.next();
            if (message == null) return null;
            if (!isOdd(data.length)) return message;
            for (int i = 0; i < data.length; i+=2)
                message.write(data[i], data[i+1]);
            return message;
        }

        private boolean isOdd(int i) {
            return (i/2)*2 == i;
        }
    }

    public static class Csv implements MessageInputStream {
        private String name;
        protected BufferedReader reader;
        protected String[] headers;

        public static MessageInputStream of(InputStream is) throws IOException {
            return new Csv(is);
        }

        protected Csv(InputStream is) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(is), 65536);
            this.headers = nextRow();
        }


        @Override
        public String name() {
            return name;
        }

        @Override
        public void name(String value) {
            this.name = value;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Message next() throws IOException {
            String[] data;
            do {
                data = nextRow();
                if (data == null) return null;
            }
            while (data.length == 0);
            Message message = new Message("");
            for (int i = 0; i < Math.min(data.length, headers.length); i++)
                message.write(headers[i].trim(), data[i].trim());
            return message;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        protected String[] nextRow() throws IOException {
            String line = reader.readLine();
            return line != null ? line.split(";") : null;
        }
    }
}
