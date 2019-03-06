package io.intino.ness.core.remote;

import io.intino.alexandria.inl.Message;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.List;

public class SequenceMessagesWriter {
	private final FileSystem hdfs;
	private final String path;

	public SequenceMessagesWriter(FileSystem hdfs, String path) {
		this.hdfs = hdfs;
		this.path = path;
	}

	public void write(String sessionName, List<Message> messages) throws IOException {
		Text key = new Text();
		Text value = new Text();
		try (SequenceFile.Writer writer = SequenceFile.createWriter(hdfs, hdfs.getConf(), new Path(path), key.getClass(), value.getClass())) {
			for (Message message : messages) {
				key.set(sessionName);
				value.set(message.toString());
				writer.append(key, value);
			}
		}
	}
}
