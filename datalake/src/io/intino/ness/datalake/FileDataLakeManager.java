package io.intino.ness.datalake;

import java.io.File;

public class FileDataLakeManager implements NessDataLake.Manager{

    private final File file;

    public FileDataLakeManager(File file) {
        this.file = file;
    }

    @Override
    public void create(String topic) {
        File file = checkExists(getTopicFile(topic));
        if (file.mkdirs()) return;
        throw new RuntimeException("Topic could not be created");
    }

    @Override
    public void remove(String topic) {
        File file = checkNotExists(getTopicFile(topic));
        if (file.listFiles() == null)
            if (!file.delete()) throw new RuntimeException("Empty topic could not be removed");
        if (file.renameTo(new File(file.getParent(), "trash." + topic))) throw new RuntimeException("Topic could not be removed");
    }

    @Override
    public void rename(String topic, String newName) {
        File file = checkNotExists(getTopicFile(topic));
        if (file.renameTo(new File(file.getParent(), newName))) throw new RuntimeException("Topic could not be renamed");
    }

    private File checkExists(File file) {
        if (file.exists()) throw new RuntimeException("Topic already exists");
        return file;
    }

    private File checkNotExists(File file) {
        if (!file.exists()) throw new RuntimeException("Topic does not exist");
        return file;
    }

    private File getTopicFile(String topic) {
        return new File(this.file, topic);
    }
}
