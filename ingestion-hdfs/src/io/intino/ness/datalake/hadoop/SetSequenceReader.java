package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SetSequenceReader {
	private final FileSystem hdfs;
	private final String path;

	SetSequenceReader(FileSystem hdfs, String path) {
		this.hdfs = hdfs;
		this.path = path;
	}

	ZetStream read() {
		try {
			SequenceFile.Reader reader = null;
			try {
				Text key = new Text();
				BytesWritable value = new BytesWritable();
				reader = new SequenceFile.Reader(hdfs.getConf(), SequenceFile.Reader.file(new Path(path)), SequenceFile.Reader.bufferSize(4096));
				reader.next(key, value);
				return new ZetReader(new ByteArrayInputStream(value.getBytes()));
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}
}
