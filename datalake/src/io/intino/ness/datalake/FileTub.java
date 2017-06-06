package io.intino.ness.datalake;

import io.intino.ness.inl.*;
import io.intino.ness.inl.FileMessageInputStream;

import java.io.*;

import static io.intino.ness.datalake.Tank.*;

public class FileTub implements Tub {

    private final File file;
    private MessageInputStream input;

    public FileTub(File file) {
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
            File feedFile = isInl(file) ? file : feedFile(file);
            File sealFile = isZip(file) ? file : sealFile(file);
            return feedFile.exists() && sealFile.exists() ?
                    FileMessageInputStream.of(sealFile, feedFile) :
                    FileMessageInputStream.of(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new MessageInputStream.Empty();
        }
    }

    private boolean isInl(File file) {
        return file.getName().endsWith(".inl");
    }

    private boolean isZip(File file) {
        return file.getName().endsWith(".zip");
    }

    private File feedFile(File file) {
        return new File(file.getAbsolutePath().replace(".zip",".inl"));
    }

    private File sealFile(File file) {
        return new File(file.getAbsolutePath().replace(".inl",".zip"));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof FileTub && file.equals(((FileTub) obj).file);
    }


}
