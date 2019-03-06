package test;

import io.intino.alexandria.inl.Message;
import io.intino.ness.Inl;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class Sorter {
	@Test
	public void sort() {
		final File inlFile = new File("/Users/oroncal/workspace/ness/application/test-res/20180405.inl");
		final List<Message> messages = Inl.load(inlFile);
		messages.sort(messageComparator());
	}

	private Comparator<Message> messageComparator() {
		return Comparator.comparing(m -> Instant.parse(tsOf(m)));
	}

	private String tsOf(Message message) {
		return message.get("ts");
	}
}