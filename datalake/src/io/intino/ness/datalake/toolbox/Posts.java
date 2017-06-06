    package io.intino.ness.datalake.toolbox;

import io.intino.ness.datalake.Post;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.util.Scanner;

public class Posts {

    public static final Post out;
    public static final Post console;

    public static Post file(String filename) throws IOException {
        return Export.to(filename);
    }

    public static Post every(int seconds) {
        return new Post() {
            long last = 0;
            long delay = 1000 * seconds;

            @Override
            public void send(Message message) {
                long ts = System.currentTimeMillis();
                if (ts - last < delay) return;
                last = ts;
                System.out.println(message);
            }

        };
    }

    public static Post progress(int length) {
        return new Post() {
            int i = 0;
            @Override
            public void send(Message message) {
                if (i++ % length != 0) return;
                System.out.print(".");
            }
        };
    }


    private static void show(Message message) {
        System.out.println(message.toString());
        new Scanner(System.in).next();
    }

    static {
        out = System.out::println;
        console = Posts::show;
    }

}
