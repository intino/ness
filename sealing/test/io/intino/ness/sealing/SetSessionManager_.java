package io.intino.ness.sealing;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.mapp.Mapp;
import io.intino.alexandria.mapp.MappReader;
import io.intino.alexandria.mapp.MappStream;
import io.intino.alexandria.zet.ZetReader;
import io.intino.ness.datalake.file.FileDatalake;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.ingestion.SetSession;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class SetSessionManager_ {
	private static final File LOCAL_STAGE = new File("temp/localstage");
	private static final File STAGE_FOLDER = new File("temp/stage");
	private static final File SESSIONS_FOLDER = new File("temp/session");
	private static final File DATALAKE = new File("temp/datalake");

	@Test
	public void should_create_and_seal_set_session() throws IOException {
		SessionHandler handler = new SessionHandler(LOCAL_STAGE);
		LocalDateTime dateTime = LocalDateTime.of(2019, 2, 28, 16, 15);
		Timetag timetag = new Timetag(dateTime, Scale.Hour);
		SetSession session = handler.createSetSession();
		for (int i = 1; i < 31; i++) session.put("tank1", timetag, "0", i);
		session.close();

		handler.pushTo(STAGE_FOLDER);
		new FileSessionManager(new FileDatalake(DATALAKE), SESSIONS_FOLDER, STAGE_FOLDER).seal();
		ZetReader reader = new ZetReader(new File("temp/datalake/sets/tank1/" + timetag.value() + "/0.zet"));
		for (int i = 1; i < 31; i++)
			assertEquals(reader.next(), i);
		File indexFile = new File("temp/datalake/sets/tank1/" + timetag.value() + "/.mapp");
		Mapp mapp = new Mapp(indexFile);
		assertEquals(mapp.size(), 30);
		MappStream.Item next = new MappReader(indexFile).next();
		assertEquals(1, next.key());
		assertEquals("0", next.value());
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

	private void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		directoryToBeDeleted.delete();
	}
}