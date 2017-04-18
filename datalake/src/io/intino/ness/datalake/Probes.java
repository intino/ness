    package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;

import java.util.Scanner;

public class Probes {
    public static final MessageMapper out = Probes::print;

    public static MessageMapper every(int seconds) {
        return new MessageMapper() {
            public long last = 0;
            public long delay = 1000 * seconds;

            @Override
            public Message map(Message message) {
                long current = System.currentTimeMillis();
                if (current - last < delay) return message;
                last = current;
                System.out.println(message.toString());
                return message;
            }
        };
    }

    public static final MessageMapper console = new MessageMapper() {
        Scanner scanner = new Scanner(System.in);
        @Override
        public Message map(Message message) {
            scanner.next();
            return print(message);
        }
    };

    public static MessageMapper progress(int length) {
        return new MessageMapper() {
            int i = 0;
            @Override
            public Message map(Message message) {
                if (i++ % length == 0) System.out.print(".");
                return message;
            }
        };
    }

    private static Message print(Message message) {
        System.out.println(message.toString());
        return message;
    }


}
