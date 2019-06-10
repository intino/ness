package io.intino.ness.sealing;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class EventSorter_ {
	@Test
	@Ignore //FIXME
	public void should_sort_blob_of_1000_messages() throws IOException {
		new EventSorter(new File("test-res/eventsort/1000.blob"), new File("temp")).sort(new File("temp/eventsort/result/1000.inl"));
	}

	@Test
	@Ignore //FIXME
	public void should_sort_blob_of_10000_messages() throws IOException {
		new EventSorter(new File("test-res/eventsort/10000.blob"), new File("temp")).sort(new File("temp/eventsort/result/10000.inl"));
	}
}
