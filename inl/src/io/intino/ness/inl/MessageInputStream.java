package io.intino.ness.inl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.sort;

public interface MessageInputStream {

    String name();
    Message next() throws IOException;
    void close() throws IOException;

    class Collection implements MessageInputStream {
        private String name;
        private Iterator<Message> iterator;

        public Collection(String name, Iterator<Message> iterator) {
            this.name = name;
            this.iterator = iterator;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Message next() throws IOException {
            return iterator.hasNext() ? iterator.next() : null;
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public String toString() {
            return name;
        }
    }

    class Sort extends Collection {

        public Sort(String name, Iterator<Message> iterator) {
            super(name, iterator);
        }

        public static MessageInputStream of(MessageInputStream... inputs) throws IOException {
            List<Message> messages = new ArrayList<>();
            String name = "sort";
            for (MessageInputStream input : inputs) {
                messages.addAll(MessageReader.readAll(input));
                name += ":"+ input.name();
            }
            sort(messages, byTs());
            return new Sort(name, messages.iterator());
        }

        private static Comparator<Message> byTs() {
            return new Comparator<Message>() {
                @Override
                public int compare(Message o1, Message o2) {
                    return o1.ts().compareTo(o2.ts());
                }
            };
        }

    }

    class Empty implements MessageInputStream {

        @Override
        public String name() {
            return "empty";
        }

        @Override
        public Message next() throws IOException {
            return null;
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public String toString() {
            return name();
        }

    }
}
