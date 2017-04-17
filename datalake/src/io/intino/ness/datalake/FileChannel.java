package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Channel;
import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.inl.FileMessageOutputStream;
import io.intino.ness.inl.Message;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.intino.ness.datalake.FileChannel.Format.zip;
import static io.intino.ness.inl.Message.empty;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileChannel implements Channel {

    private final File folder;
    private final Joint joint;
    private final Writer writer;

    public enum Format {
        zip, inl
    }

    public FileChannel(File folder) {
        this(folder, null);
    }

    public FileChannel(File folder, Joint joint) {
        this.folder = folder;
        this.joint = joint;
        this.writer = new Writer();
    }

    @Override
    public String name() {
        return folder.getName();
    }

    @Override
    public List<Reservoir> reservoirs() {
        return stream(files())
                .sorted(File::compareTo)
                .map(this::reservoir)
                .collect(toList());
    }

    private Reservoir reservoir(File file) {
        return file.isFile() ? new FileReservoir(file) : new FolderReservoir(file, joint);
    }

    @Override
    public Reservoir get(Instant instant) {
        return new FileReservoir(fileOf(instant, zip));
    }


    private File[] files() {
        File[] files = folder.listFiles();
        return files != null ? sort(files) : new File[0];
    }

    private File[] sort(File[] files) {
        Arrays.sort(files);
        return files;
    }

    File[] files(Format format) {
        return folder.listFiles(f -> f.getName().endsWith(format.name()));
    }

    File fileOf(Message message, Format format) {
        return new File(folder, dayOf(message.ts()) + "." + format.name());
    }

    File fileOf(Instant instant, Format format) {
        return new File(folder, dayOf(instant.toString()) + "." + format.name());
    }

    void create() {
        if (folder.mkdirs()) return;
        throw new RuntimeException("Channel could not be created");
    }

    boolean exists() {
        return folder.exists();
    }

    void rename(String newName) {
        if (rename(folder, newName)) return;
        throw new RuntimeException("Channel could not be renamed");
    }

    void remove() {
        if (isEmpty()) {
            if (folder.delete()) return;
            throw new RuntimeException("Empty channel could not be removed");
        }
        if (rename(folder, "trash." + uid() + "." + folder.getName())) return;
        throw new RuntimeException("Channel could not be removed");

    }

    private String uid() {
        return UUID.randomUUID().toString();
    }

    private boolean rename(File file, String newName) {
        return file.renameTo(new File(file.getParent(), newName));
    }

    private boolean isEmpty() {
        return folder.listFiles() == null;
    }

    private static String dayOf(String instant) {
        return instant.replace("-", "").substring(0, 8);
    }

    void write(Message message)  {
        if (message == empty()) return;
        try {
            this.writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close()  {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Writer {
        private File file;
        private FileMessageOutputStream os = null;

        public void write(Message message) throws IOException {
            open(message);
            os.write(message);
        }

        private void open(Message message) throws IOException {
            File file = fileOf(message, zip);
            if (file.equals(this.file)) return;
            close();
            this.file = file;
            this.os = FileMessageOutputStream.of(file);
        }

        public void close() throws IOException {
            if (this.os == null) return;
            try {
                this.os.close();
            }
            finally {
                this.os = null;
            }
        }

    }


}
