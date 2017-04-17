package io.intino.ness.datalake;

import java.io.File;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileDataLake implements NessDataLake {
    private final File folder;

    public FileDataLake(String folder) {
        this(new File(folder));
    }

    public FileDataLake(File folder) {
        this.folder = folder;
    }

    @Override
    public List<Channel> channels() {
        return stream(files())
                .map(FileChannel::new)
                .collect(toList());
    }

    @Override
    public Channel get(String channel) {
        return new FileChannel(folderOf(channel));
    }

    private File folderOf(String channel) {
        return new File(this.folder, channel);
    }

    private File[] files() {
        File[] files = folder.listFiles();
        return files != null ? files : new File[0];
    }

}
