package io.intino.ness.datalake.toolbox;

import io.intino.ness.datalake.Post;
import io.intino.ness.inl.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Export implements Post {

    private List<String> header;
    private final BufferedWriter writer;

    private Export(File file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
        header = new ArrayList<>();
    }

    public static Post to(String filename) throws IOException {
        if (!filename.endsWith(".csv")) throw new RuntimeException("Only export to csv files is supported");
        return new Export(new File(filename));
    }

    @Override
    public void send(Message message) {
        if (message == Message.empty) return;
        if (header.isEmpty()) loadHeaderOf(message);
        StringBuilder line = new StringBuilder();
        for (String attribute : header) line.append(";").append(message.read(attribute));
        write(line.substring(1));
    }

    private void loadHeaderOf(Message message) {
        header.addAll(message.attributes());
        StringBuilder line = new StringBuilder();
        for (String attribute : header) line.append(";").append(attribute);
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


}
