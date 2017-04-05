package io.intino.ness.datalake.filesystem;

import io.intino.ness.datalake.NessDataLake;
import io.intino.ness.inl.*;

import java.io.*;

import static java.util.Arrays.stream;

public class FileReservoir implements NessDataLake.Reservoir {

    private final File file;
    private MessageInputStream[] inputStreams;
    private MessageOutputStream outputStream;

    public FileReservoir(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public MessageInputStream[] inputStreams() {
        if (inputStreams != null) return inputStreams;
        return inputStreams = createInputStreams();
    }

    @Override
    public MessageOutputStream outputStream() {
        if (outputStream != null) return outputStream;
        return outputStream = createOutputStream();
    }

    private MessageOutputStream createOutputStream() {
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
