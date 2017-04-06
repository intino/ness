package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.datalake.NessDataLake.Topic;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileTopic implements Topic {
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
        return stream(files())
                .map(FileReservoir::new)
                .sorted(FileReservoir::compareTo)
                .collect(toList());
    }

    @Override
    public Reservoir get(Instant instant) {
        return new FileReservoir(fileOf(instant));
    }

    private File fileOf(Instant instant) {
        return new File(file, filenameOf(instant));
    }

    private String filenameOf(Instant instant) {
        return instant.toString().replace("-", "").substring(0, 8) + ".zip";
    }

    private File[] files() {
        File[] files = file.listFiles();
        return files != null ? sort(files) : new File[0];
    }

    private File[] sort(File[] files) {
        Arrays.sort(files);
        return files;
    }

}
