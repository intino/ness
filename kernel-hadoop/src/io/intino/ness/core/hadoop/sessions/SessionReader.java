package io.intino.ness.core.hadoop.sessions;

import io.intino.alexandria.logger.Logger;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.hadoop.io.SequenceFile.Reader;

public class SessionReader {

	private final FileSystem fs;
	private final Path path;

	public SessionReader(FileSystem fs, Path path) {
		this.fs = fs;
		this.path = path;
	}

	public Map<String, byte[]> read() {
		Map<String, byte[]> map = new HashMap<>();
		try {
			Reader reader = null;
			try {
				Text key = new Text();
				BytesWritable value = new BytesWritable();
				reader = new Reader(fs.getConf(), Reader.file(new Path(fs.getUri().toString() + "/" + path)), Reader.bufferSize(4096));
				while (reader.next(key, value)) map.put(key.toString(), value.getBytes());
			} finally {
				if (reader != null) reader.close();
			}
			return map;
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyMap();
		}
	}
}