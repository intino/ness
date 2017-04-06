package io.intino.ness.inl;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortingInputStream extends InputStream {
    private final BufferedReader reader;
    private InputStream is;
    private String header;
    private int size;

    public SortingInputStream(InputStream is)  {
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.is = new ByteArrayInputStream(this.readAll());
    }

    private byte[] readAll()  {
        try {
            return serialize(blocks());
        }
        catch (IOException e) {
            return new byte[0];
        }
    }

    private byte[] serialize(List<Block> blocks) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);
        for (Block block : blocks) {
            os.write((header+"\n").getBytes());
            os.write((block.ts+"\n"+block.data+"\n").getBytes());
        }
        os.close();
        return os.toByteArray();
    }

    private List<Block> blocks() throws IOException {
        List<Block> blocks = new ArrayList<>();
        header = nextLine();
        while (true) {
            Block block = readBlock();
            if (block == null) break;
            blocks.add(block);
        }
        Collections.sort(blocks, byTimeStamp());
        return blocks;
    }

    private String nextLine() throws IOException {
        String line = reader.readLine();
        size += line.length()+1;
        return line;
    }

    private Comparator<Block> byTimeStamp() {
        return new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                return o1.ts.compareTo(o2.ts);
            }
        };
    }


    private Block readBlock() throws IOException {
        String line = nextLine();
        if (line == null) return null;
        Block block = new Block(line);
        while (true) {
            line = nextLine();
            if (line == null || line.equalsIgnoreCase(header)) return block;
            block.data += line + "\n";
        }
    }

    private static class Block {
        String ts;
        String data;

        public Block(String ts) {
            this.ts = ts;
            this.data = "";
        }
    }


    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        is.reset();
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }
}
