package io.intino.ness.inl;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileMessageOutputStream implements MessageOutputStream {
    private final ZipOutputStream os;

    public FileMessageOutputStream(File file) throws IOException {
        file.getParentFile().mkdir();
        this.os = new ZipOutputStream(new FileOutputStream(file));
        this.os.putNextEntry(new ZipEntry("events.inl"));
    }

    @Override
    public void write(Message message) throws IOException {
        String data = message.toString() + "\n\n";
        os.write(data.getBytes());
    }


    @Override
    public void close() throws IOException {
        os.closeEntry();
        os.close();
    }
}
