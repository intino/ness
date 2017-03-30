package io.intino.ness.datalake.filesystem;

import io.intino.ness.datalake.NessDataLake;
import io.intino.ness.datalake.NessDataLake.Reservoir;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

public class FileTopic implements NessDataLake.Topic {

    private final File file;

    public FileTopic(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public List<Reservoir> reservoirs() {
        List<File> files = asList(files());
        files.sort(File::compareTo);
        return files.stream().map(FileReservoir::new).collect(toList());
    }

    @Override
    public Reservoir get(Instant instant) {
        return new FileReservoir(fileOf(instant));
    }

    private File fileOf(Instant instant) {
        return new File(this.file, filenameOf(instant));
    }

    private String filenameOf(Instant instant) {
        return instant.toString().replace("-", "").substring(0, 8) + ".zip";
    }

    private File[] files() {
        File[] files = file.listFiles();
        return files != null ? files : new File[0];
    }

}
