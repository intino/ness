package io.intino.ness.core.remote;

import io.intino.alexandria.zet.ZOutputStream;
import io.intino.alexandria.zet.ZetStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SequenceSetWriter {
	private final FileSystem fs;
	private final String path;

	SequenceSetWriter(FileSystem fs, String path) {
		this.fs = fs;
		this.path = path;
	}

	void write(ZetStream stream) throws IOException {
		Text key = new Text();
		BytesWritable value = new BytesWritable();
		try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, fs.getConf(), new Path(fs.getUri().toString() + "/" + path), key.getClass(), value.getClass())) {
			key.set(path);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ZOutputStream zstream = new ZOutputStream(bytes);
			while (stream.hasNext()) zstream.writeLong(stream.next());
			value.set(bytes.toByteArray(), 0, bytes.size());
			writer.append(key, value);
			writer.sync();
		}
	}
}
