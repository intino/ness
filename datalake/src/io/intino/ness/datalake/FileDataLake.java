package io.intino.ness.datalake;

import java.io.File;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileDataLake implements NessDataLake {
    private final File file;

    public FileDataLake(String file) {
        this(new File(file));
    }

    public FileDataLake(File file) {
        this.file = file;
    }

    @Override
    public List<Topic> topics() {
        return stream(files())
                .map(FileTopic::new)
                .collect(toList());
    }

    @Override
    public Topic get(String topic) {
        return new FileTopic(fileOf(topic));
    }

    @Override
    public Manager manage() {
        return new FileDataLakeManager(file);
    }

    private File fileOf(String topic) {
        return new File(this.file, topic);
    }

    private File[] files() {
        File[] files = file.listFiles();
        return files != null ? files : new File[0];
    }

}
