package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.Zet;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.datalake.hadoop.sessions.SetSessionReader;
import io.intino.ness.ingestion.Fingerprint;
import io.intino.ness.ingestion.Session;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionSealer {
	private final FileSystem fs;
	private final HadoopStage stage;
	private final Path events;
	private final Path sets;
	private final Path temp;

	public SessionSealer(FileSystem fs, HadoopStage stage, Path eventStore, Path setStore, Path temp) {
		this.fs = fs;
		this.stage = stage;
		this.events = eventStore;
		this.sets = setStore;
		this.temp = temp;
	}

	public void seal() {
		try {
			Job job = Job.getInstance();
			job.setJarByClass(this.getClass());
			job.setJobName("Sealer");
			FileInputFormat.addInputPath(job, stage.path());
			job.setInputFormatClass(SequenceFileInputFormat.class);
			FileOutputFormat.setOutputPath(job, sets);
			job.setMapperClass(BlobMapper.class);
			job.setCombinerClass(ChunksCombiner.class);
			job.setReducerClass(BlobReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(BytesWritable.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.addCacheFile(sets.toUri());
			job.addCacheFile(events.toUri());
			job.setNumReduceTasks(2);
			job.waitForCompletion(true);
			if (job.isSuccessful()) {
				System.out.println("Job was successful");
			} else if (!job.isSuccessful()) {
				System.out.println("Job was not successful");
			}
		} catch (IOException | InterruptedException | ClassNotFoundException e) {
			Logger.error(e);
		}
	}

	public static class BlobMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {

		private final BytesWritable zet = new BytesWritable();

		private Text fingerprint = new Text();

		@Override
		protected void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
			if (key.toString().startsWith(Session.Type.event.name())) {
				mapEventBlob(value.getBytes());
			} else {
				Map<String, byte[]> fingerprintMap = mapSetBlobToZetStreams(value.getBytes());
				for (String fp : fingerprintMap.keySet()) {
					fingerprint.set(fp);
					byte[] bytes = fingerprintMap.get(fp);
					zet.set(bytes, 0, bytes.length);
					context.write(fingerprint, zet);
				}
			}
		}

		private Map<String, byte[]> mapSetBlobToZetStreams(byte[] bytes) {
			try {
				SetSessionReader reader = new SetSessionReader(bytes);
				Map<String, byte[]> chunks = new HashMap<>();
				for (Fingerprint fingerprint : reader.fingerprints())
					chunks.put(fingerprint.toString(), merge(reader.streamsOf(fingerprint)));
				return chunks;
			} catch (IOException e) {
				Logger.error(e);
				return Collections.emptyMap();
			}
		}


		private void mapEventBlob(byte[] bytes) {
		}


		private byte[] merge(List<ZetStream> zetStreams) {
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			long[] ids = new Zet(new ZetStream.Merge(zetStreams)).ids();
			for (long id : ids) buffer.putLong(id);
			return buffer.array();
		}

	}

	public static class ChunksCombiner extends Reducer<Text, BytesWritable, Text, BytesWritable> {
		private final BytesWritable zet = new BytesWritable();

		private Text fingerprint = new Text();

		@Override
		protected void reduce(Text key, Iterable<BytesWritable> values, Context context) throws IOException, InterruptedException {
			ZetStream sink = new ZetReader();
			for (BytesWritable value : values) {
				ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).put(value.getBytes(), 0, value.getLength());
				sink = new ZetStream.Merge(sink, new ZetReader(buffer.asLongBuffer().array()));
			}
			ByteBuffer result = ByteBuffer.allocate(Long.BYTES);
			for (long id : new Zet(sink).ids()) result.putLong(id);
			zet.set(result.array(), 0, result.array().length);
			context.write(fingerprint, zet);
		}

	}

	public static class BlobReducer extends Reducer<Text, BytesWritable, Text, BytesWritable> {
		private final BytesWritable zet = new BytesWritable();
		private Text fingerprint = new Text();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);

		}

		@Override
		protected void reduce(Text key, Iterable<BytesWritable> values, Context context) throws IOException, InterruptedException {
			Fingerprint fingerprint = new Fingerprint(key.toString());
			Path destination = fileOf(context.getConfiguration(), fingerprint, new Path(context.getCacheFiles()[0]));
			Reader reader = new Reader(context.getConfiguration(), Reader.file(destination));
			byte[] bytes = values.iterator().next().getBytes();
			//TODO merge

		}

		private Path fileOf(Configuration conf, Fingerprint fingerprint, Path sets) {
			Path path = new Path(sets, fingerprint.tank() + "/" + fingerprint.timetag() + ".seq");
			try {
				FileSystem.get(conf).mkdirs(path);
			} catch (IOException e) {
				Logger.error(e);
			}
			return path;
		}

	}
}
