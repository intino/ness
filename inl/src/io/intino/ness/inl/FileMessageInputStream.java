package io.intino.ness.inl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static io.intino.ness.inl.FileMessageInputStream.Format.*;
import static io.intino.ness.inl.MessageInputStream.*;

public class FileMessageInputStream {

    public static MessageInputStream of(File file) {
        Format format = formatOf(file);
        if (format == inz) return new Inz(streamOf(file));
        if (format == inf) return new Inz(new SortingInputStream(streamOf(file)));
        if (format == csv) return new Csv(streamOf(file));
        if (format == tsv) return new Tsv(streamOf(file));
        if (format == dat) return new Dat(streamOf(file));
        return messageInputStream();
    }


    private static InputStream streamOf(File file) {
        try {
            return isZip(file) ?
                    zipStreamOf(file) :
                    new FileInputStream(file);
        } catch (IOException e) {
            return emptyInputStream();
        }
    }

    private static boolean isZip(File file) {
        return file.getName().endsWith(".zip");
    }

    private static InputStream zipStreamOf(File file) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
        zipStream.getNextEntry();
        return zipStream;
    }

    private static ByteArrayInputStream emptyInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static MessageInputStream messageInputStream() {
        return new MessageInputStream() {
            @Override
            public Message next() {
                return null;
            }
        };
    }

    private static Format formatOf(File file) {
        String name = file.getName().toLowerCase();
        for (String extension : formats.keySet())
            if (name.endsWith(extension)) return formats.get(extension);
        return unknown;
    }

    private static Map<String, Format> formats = new HashMap<>();

    public enum Format {
        inz, inf, csv, dat, tsv, unknown
    }

    static {
        formats.put(".zip", inz);
        formats.put(".feed.zip", inf);
        formats.put(".csv", csv);
        formats.put(".dat", dat);
    }



}
