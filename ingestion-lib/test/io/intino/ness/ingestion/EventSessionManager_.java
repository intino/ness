package io.intino.ness.ingestion;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimBuilder;
import io.intino.alexandria.zim.ZimReader;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class EventSessionManager_ {

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

	@Test
	public void should_create_an_event_session() {
		deleteDirectory(new File("temp"));
		Instant instant = Instant.parse("2018-09-10T00:00:00Z");
		File file = new File("temp/events/tank1/201809" + ZimReader.ZimExtension);
		assertTrue(file.exists());
		ZimReader zimReader = new ZimReader(file);
		assertEquals(message(instant, 0).toString(), zimReader.next().toString());
		assertFalse(zimReader.hasNext());
	}

	@Test
	public void should_create_an_event_session_and_merge_when_sealing_it() {
		ZimBuilder writer = new ZimBuilder(new File("temp/events/tank1/201809.zim"));
		Instant instant = Instant.parse("2018-09-09T00:00:00Z");
		writer.put(message(instant, -1));
		File file = new File("temp/events/tank1/201809" + ZimReader.ZimExtension);
		assertTrue(file.exists());
		ZimReader zimReader = new ZimReader(file);
		assertEquals(message(instant, -1).toString(), zimReader.next().toString());
		assertEquals(message(Instant.parse("2018-09-10T00:00:00Z"), 0).toString(), zimReader.next().toString());
		assertFalse(zimReader.hasNext());
	}

	private Stream<Session> sessions() {
		return FS.filesIn(new File("test-res/eventstage"), f -> true)
				.map(file -> new Session() {
					@Override
					public String name() {
						String name = file.getName();
						return name.substring(0, name.lastIndexOf("."));
					}

					@Override
					public Type type() {
						return Type.event;
					}

					@Override
					public InputStream inputStream() {
						try {
							return new FileInputStream(file);
						} catch (IOException e) {
							Logger.error(e);
							return null;
						}
					}
				});
	}

	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("entries", index);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

}