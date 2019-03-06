package io.intino.ness.core.remote;

import io.intino.alexandria.logger.Logger;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;
import java.util.UUID;

public class SessionSealer {
	private final FileSystem fs;
	private final HadoopStage stage;
	private final Path events;
	private final Path sets;
	private final Path temp;

	public SessionSealer(FileSystem fs, HadoopStage stage, Path events, Path sets, Path temp) {
		this.fs = fs;
		this.stage = stage;
		this.events = events;
		this.sets = sets;
		this.temp = temp;
	}

	public void seal() {
		try {
			Job job = Job.getInstance().;
			job.setJarByClass(this.getClass());
			job.setJobName("Sealer");
			FileInputFormat.addInputPath(job, stage.path());
			job.setInputFormatClass(SequenceFileInputFormat.class);

			FileOutputFormat.setOutputPath(job, temp);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(BytesWritable.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.setNumReduceTasks(0);
			int returnValue = job.waitForCompletion(true) ? 0 : 1;
			if (job.isSuccessful()) {
				System.out.println("Job was successful");
			} else if (!job.isSuccessful()) {
				System.out.println("Job was not successful");
			}

			return returnValue;
		} catch (IOException | InterruptedException | ClassNotFoundException e) {
			Logger.error(e);
		}
	}
}
