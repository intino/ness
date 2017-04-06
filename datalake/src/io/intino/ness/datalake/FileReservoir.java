package io.intino.ness.datalake;

import io.intino.ness.inl.*;

import java.io.*;

import static java.util.Arrays.stream;

public class FileReservoir implements NessDataLake.Reservoir {

    private final File file;
    private MessageInputStream[] inputs;
    private MessageOutputStream output;
    private MessageOutputStream feed;

    public FileReservoir(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public MessageInputStream[] inputs() {
        if (inputs != null) return inputs;
        return inputs = createInputStreams();
    }

    @Override
    public MessageOutputStream output() {
        if (output != null) return output;
        return output = createOutputStream(file);
    }

    @Override
    public MessageOutputStream feed() {
        if (feed != null) return feed;
        return feed = createOutputStream(feed(file));
    }

    private File feed(File file) {
        return new File(file.getAbsolutePath().replace("\\.zip",".feeding.zip"));
    }

    private MessageOutputStream createOutputStream(File file) {
        try {
            return new FileMessageOutputStream(file);
        } catch (IOException e) {
            return null;
        }
    }

    private MessageInputStream[] createInputStreams() {
        return stream(files())
                .map(FileMessageInputStream::of)
                .toArray(MessageInputStream[]::new);
    }

    private File[] files() {
        return file.isDirectory() ? array(file.listFiles()) : new File[]{file};
    }

    private File[] array(File[] files) {
        return files != null ? files : new File[0];
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof FileReservoir && file.equals(((FileReservoir) obj).file);
    }

    public int compareTo(FileReservoir reservoir) {
        return file.compareTo(reservoir.file);
    }

}
