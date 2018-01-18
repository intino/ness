package io.intino.ness.datalake;

import io.intino.ness.inl.MessageInputStream.Sort;
import io.intino.ness.inl.streams.FileMessageInputStream;

import java.io.File;
import java.io.IOException;

public class MessageInputStream {


	public static io.intino.ness.inl.MessageInputStream of(File... files) throws IOException {
		io.intino.ness.inl.MessageInputStream[] inputStreams = new io.intino.ness.inl.MessageInputStream[files.length];
		for (int i = 0; i < files.length; i++) inputStreams[i] = FileMessageInputStream.of(files[i]);
		return Sort.of(inputStreams);
	}
}
