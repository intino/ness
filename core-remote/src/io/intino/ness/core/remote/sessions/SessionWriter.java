package io.intino.ness.core.remote.sessions;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Session;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.IOException;

public class SessionWriter {

	private final FileSystem fs;
	private final Path path;

	public SessionWriter(FileSystem fs, Path path) {
		this.fs = fs;
		this.path = path;
	}

	public void write(Session session) {
		Text key = new Text();
		BytesWritable value = new BytesWritable();
		try (SequenceFile.Writer writer = SequenceFile.createWriter(fs, fs.getConf(), new Path(fs.getUri().toString() + "/" + path), key.getClass(), value.getClass())) {
			key.set(session.type().name() + "#" + session.name());
			byte[] bytes = IOUtils.toByteArray(session.inputStream());
			value.set(bytes, 0, bytes.length);
			writer.append(key, value);
			writer.sync();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
