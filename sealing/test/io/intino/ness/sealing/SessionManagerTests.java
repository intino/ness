package io.intino.ness.sealing;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.mapp.Mapp;
import io.intino.alexandria.mapp.MappReader;
import io.intino.alexandria.mapp.MappStream;
import io.intino.alexandria.zet.ZetReader;
import io.intino.alexandria.zim.ZimReader;
import io.intino.ness.datalake.file.FileDatalake;
import io.intino.ness.ingestion.EventSession;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.ingestion.SetSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class SessionManagerTests {

	private static final File STAGE_FOLDER = new File("temp/stage");
	private static final File LOCAL_STAGE = new File("temp/localstage");
	private static final File SESSIONS_FOLDER = new File("temp/session");
	private static final File DATALAKE = new File("temp/datalake");

	@Test
	public void should_create_a_session() throws IOException {
		SessionHandler handler = new SessionHandler(LOCAL_STAGE);
		List<Message> messageList = createEvents(handler);
		Timetag timetag = createSets(handler);

		handler.pushTo(STAGE_FOLDER);
		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(DATALAKE), SESSIONS_FOLDER, STAGE_FOLDER);
		fileSessionManager.seal();

		checkEvents(messageList);
		checkSets(timetag);
	}

	private Timetag createSets(SessionHandler handler) {
		LocalDateTime dateTime = LocalDateTime.of(2019, 2, 28, 16, 15);
		Timetag timetag = new Timetag(dateTime, Scale.Hour);
		SetSession setSession = handler.createSetSession();
		for (int i = 1; i < 31; i++) setSession.put("tank1", timetag, "0", i);
		setSession.define("tank1", timetag, "0", "var", "value");
		setSession.close();
		return timetag;
	}

	private List<Message> createEvents(SessionHandler handler) {
		EventSession eventSession = handler.createEventSession();
		List<Message> messageList = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			LocalDateTime now = LocalDateTime.of(2019, 02, 28, 04, 15 + i);
			Message message = message(now.toInstant(ZoneOffset.UTC), i);
			messageList.add(message);
			eventSession.put("tank1", new Timetag(now, Scale.Hour), message);
		}
		eventSession.close();
		return messageList;
	}

	private void checkEvents(List<Message> messageList) {
		ZimReader reader = new ZimReader(new File("temp/datalake/events/tank1/2019022804.zim"));
		for (int i = 0; i < 30; i++) {
			Message next = reader.next();
			assertEquals(next.get("ts"), messageList.get(i).get("ts"));
			assertEquals(next.get("entries"), messageList.get(i).get("entries"));
		}
	}

	private void checkSets(Timetag timetag) throws IOException {
		ZetReader reader = new ZetReader(new File("temp/datalake/sets/tank1/" + timetag.value() + "/0.zet"));
		for (int i = 1; i < 31; i++)
			Assert.assertEquals(reader.next(), i);
		File indexFile = new File("temp/datalake/sets/tank1/" + timetag.value() + "/.mapp");
		Mapp mapp = new Mapp(indexFile);
		Assert.assertEquals(mapp.size(), 30);
		MappStream.Item next = new MappReader(indexFile).next();
		Assert.assertEquals(1, next.key());
		Assert.assertEquals("0", next.value());
		String line = Files.readAllLines(new File("temp/datalake/sets/tank1/" + timetag.value() + "/.metadata").toPath()).get(0);
		assertEquals(line, "0;var;value");
	}


	//TODO: merge with existing event files
	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("entries", index);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

	private void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

}
