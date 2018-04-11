package test;

import io.intino.konos.alexandria.Inl;
import io.intino.ness.datalake.MessageExternalSorter;
import io.intino.ness.inl.Message;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class Sorter {


	@Test
	public void sort() throws IOException {
		final File inlFile = new File("/Users/oroncal/workspace/ness/application/test-res/20180405.inl");
		final List<Message> messages = Inl.load(new String(readFile(inlFile), Charset.forName("UTF-8")));
		messages.sort(messageComparator());
	}

	private byte[] readFile(File inlFile) throws IOException {
		return Files.readAllBytes(inlFile.toPath());
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}
}