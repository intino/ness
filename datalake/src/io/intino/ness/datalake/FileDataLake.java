package io.intino.ness.datalake;

import java.io.File;

import static java.util.Arrays.stream;

public class FileDataLake implements NessDataLake {
    private final File folder;

    public FileDataLake(String folder) {
        this(new File(folder));
    }

    public FileDataLake(File folder) {
        this.folder = folder;
    }

    @Override
    public Tank[] tanks() {
        return stream(files())
                .map(FileTank::new)
                .toArray(Tank[]::new);
    }

    @Override
    public Tank get(String tank) {
        return new FileTank(folderOf(tank));
    }

    private File folderOf(String channel) {
        return new File(this.folder, channel);
    }

    private File[] files() {
        File[] files = folder.listFiles();
        return files != null ? files : new File[0];
    }

}
