package io.intino.ness.datalake;

import io.intino.ness.datalake.NessPumpingStation.Pipe;
import io.intino.ness.inl.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Pipes {
    public static final Pipe out = message -> System.out.println(message.toString() + "\n\n");

    public static final Pipe console = new Pipe() {
        Scanner scanner = new Scanner(System.in);
        @Override
        public void send(Message message) {
            out.send(message);
            scanner.next();
        }
    };

    public static Pipe progress(int length) {
        return new Pipe() {
            int i = 0;
            @Override
            public void send(Message message) {
                if (i++ % length == 0) System.out.print(".");
            }
        };
    }

    public static Pipe csv(String filename) throws IOException {
        return csv(new File(filename));
    }

    public static Pipe csv(File file) throws IOException {
        List<String> header = new ArrayList<>();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        return new Pipe() {
            @Override
            public void send(Message message) {
                if (header.isEmpty()) loadHeaderOf(message);
                String line = "";
                for (String attribute : header) line += ";" + message.read(attribute);
                write(line.substring(1));
            }

            private void loadHeaderOf(Message message) {
                header.addAll(message.attributes());
                String line = "";
                for (String attribute : header) line += ";" + attribute;
                write(line.substring(1));
            }

            private void write(String line) {
                try {
                    writer.write(line + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void flush() {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
    }

}
