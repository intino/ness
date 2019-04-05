package io.intino.ness.ingestion;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zim.ZimReader;
import io.intino.ness.datalake.file.FileDatalake;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

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
		List<Message> messageList = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			LocalDateTime now = LocalDateTime.of(2019, 02, 28, 16, 15 + i);
			Message message = message(now.toInstant(ZoneOffset.UTC), i);
			messageList.add(message);
			session.put("tank1", new Timetag(now, Scale.Hour), message);
		}
		session.close();
		Digester digester = new Digester(new FileDatalake(new File("temp/datalake")), new File("temp/session"));
		digester.push(handler.sessions());
		digester.seal();
		ZimReader reader = new ZimReader(new File("temp/datalake/events/tank1/2019022816.zim"));
		for (int i = 0; i < 30; i++) {
			Message next = reader.next();
			assertEquals(next.get("ts"), messageList.get(i).get("ts"));
			assertEquals(next.get("entries"), messageList.get(i).get("entries"));
		}
	}

	//TODO: merge with existing event files

	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("entries", index);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

}