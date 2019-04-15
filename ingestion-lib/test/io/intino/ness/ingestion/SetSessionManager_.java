package io.intino.ness.ingestion;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.mapp.Mapp;
import io.intino.alexandria.mapp.MappReader;
import io.intino.alexandria.mapp.MappStream;
import io.intino.alexandria.zet.ZetReader;
import io.intino.ness.datalake.file.FileDatalake;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class SetSessionManager_ {

	@Test
	public void should_create_and_seal_set_session() throws IOException {
		SessionHandler handler = new SessionHandler(new File("localstage"));
		LocalDateTime dateTime = LocalDateTime.of(2019, 2, 28, 16, 15);
		Timetag timetag = new Timetag(dateTime, Scale.Hour);
//		SetSession session = handler.createSetSession();
//		for (int i = 1; i < 31; i++) session.put("tank1", timetag, "0", i);
//		session.close();

		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(new File("temp/datalake")), new File("temp/session"));
		fileSessionManager.push(handler.sessions());
		fileSessionManager.seal();
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
//		deleteDirectory(new File("temp"));
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