package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.*;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.stream;

public class FolderReservoir implements NessDataLake.Reservoir {

    private File folder;
    private Joint joint;

    public FolderReservoir(File folder, Joint joint) {
        if (joint == null) throw new RuntimeException(folder.getAbsolutePath() + " is a folder reservoir that requires a joint");
        this.folder = folder;
        this.joint = joint;
    }

    @Override
    public String name() {
        return folder.getName();
    }

    @Override
    public MessageInputStream input() {
        return joint.join(inputStreams());
    }

    private MessageInputStream[] inputStreams() {
        return stream(files())
                .map(this::inputStream)
                .toArray(MessageInputStream[]::new);
    }

    private MessageInputStream inputStream(File file) {
        try {
            return FileMessageInputStream.of(file);
        } catch (IOException e) {
            e.printStackTrace();
            return new MessageInputStream.Empty();
        }
    }

    private File[] files() {
        return folder.listFiles();
    }



}
