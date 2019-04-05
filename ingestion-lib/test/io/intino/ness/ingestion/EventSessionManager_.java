package io.intino.ness.ingestion;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.ness.datalake.FileDatalake;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class EventSessionManager_ {

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

	@Test
	public void should_create_an_event_session() {
		SessionHandler handler = new SessionHandler();
		EventSession session = handler.createEventSession();
		for (int i = 0; i < 100; i++) {
			LocalDateTime now = LocalDateTime.now();
			session.put("tank1", new Timetag(now, Scale.Day), message(now.toInstant(ZoneOffset.UTC), i));
		}
		session.close();
		Digester digester = new Digester(new FileDatalake(new File("temp/datalake")), new File("temp/session"));
		digester.push(handler.sessions());
		digester.seal();
	}

	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("entries", index);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

}