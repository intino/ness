package io.intino.ness.datalake.filesystem;

import io.intino.ness.datalake.NessDataLake;
import io.intino.ness.datalake.NessDataLake.Format;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.intino.ness.datalake.NessDataLake.Format.*;

public class FileReservoir implements NessDataLake.Reservoir {

    private final File file;
    private InputStream inputStream;
    private OutputStream outputStream;

    public FileReservoir(File file) {
        this.file = file;
    }

    @Override
    public String name() {
        return file.getName();
    }

    @Override
    public Format format() {
        return isZip() ? inl : isCsv() ? csv : unknown;
    }

    private boolean isZip() {
        return file.getName().endsWith(".zip");
    }

    private boolean isCsv() {
        return file.getName().endsWith(".csv");
    }

    @Override
    public InputStream inputStream() {
        if (inputStream != null) return inputStream;
        return inputStream = buffer(createInputStream());
    }

    @Override
    public OutputStream outputStream() {
        if (outputStream != null) return outputStream;
        try {
            return outputStream = buffer(createOutputStream());
        } catch (IOException e) {
            return null;
        }
    }

    private InputStream createInputStream() {
        if (isZip()) return createZipInputStream();
        if (isCsv()) return createCsvInputStream();
        return emptyInputStream();
    }

    private OutputStream createOutputStream() throws IOException {
        file.getParentFile().mkdir();
        ZipOutputStream zos = new ZipOutputStream(buffer(new FileOutputStream(file)));
        zos.putNextEntry(new ZipEntry("events.inl"));
        return zos;
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        }
        catch (IOException ignored) {
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof FileReservoir) return file.equals(((FileReservoir) obj).file);
        return false;
    }

    private InputStream buffer(InputStream stream) {
        return new BufferedInputStream(stream, 1204 * 1024);
    }

    private InputStream createZipInputStream() {
        try {
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file));
            zipStream.getNextEntry();
            return zipStream;
        } catch (IOException e) {
            return emptyInputStream();
        }
    }

    private InputStream createCsvInputStream() {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            return emptyInputStream();
        }
    }

    private OutputStream buffer(OutputStream stream) {
        return new BufferedOutputStream(stream, 1204 * 1024);
    }

    private InputStream emptyInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

}
