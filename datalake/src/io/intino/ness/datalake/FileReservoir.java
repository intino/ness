package io.intino.ness.datalake;

import io.intino.ness.inl.*;

import java.io.*;

public class FileReservoir implements NessDataLake.Reservoir {

    private final File file;
    private MessageInputStream input;

    public FileReservoir(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public MessageInputStream input() {
        if (input != null) return input;
        return input = createInputStream();
    }

    private MessageInputStream createInputStream() {
        return messageInputStreamOf(file);
    }

    private MessageInputStream messageInputStreamOf(File file) {
        try {
            return FileMessageInputStream.of(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new MessageInputStream.Empty();
        }
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
