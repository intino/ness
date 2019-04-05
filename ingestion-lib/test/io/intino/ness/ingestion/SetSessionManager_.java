package io.intino.ness.ingestion;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.zet.ZetReader;
import io.intino.ness.datalake.file.FileDatalake;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;

public class SetSessionManager_ {

	@Test
	public void should_create_and_seal_set_session() {
		SessionHandler handler = new SessionHandler();
		SetSession session = handler.createSetSession();
		LocalDateTime dateTime = LocalDateTime.of(2019, 02, 28, 16, 15);
		Timetag timetag = new Timetag(dateTime, Scale.Hour);
		for (int i = 1; i < 3330; i++) session.put("tank1", timetag, "0", i);
		session.close();
		FileSessionManager fileSessionManager = new FileSessionManager(new FileDatalake(new File("temp/datalake")), new File("temp/session"));
		fileSessionManager.push(handler.sessions());
		fileSessionManager.seal();
		ZetReader reader = new ZetReader(new File("temp/datalake/sets/tank1/" + timetag.value() + "/0.zet"));
		for (int i = 1; i < 30; i++)
			TestCase.assertEquals(reader.next(), i);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
}