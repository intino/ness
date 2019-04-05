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

	private static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	@Test
	public void should_create_and_seal_set_session() {
		SessionHandler handler = new SessionHandler();
		SetSession session = handler.createSetSession();
		for (int i = 1; i < 30; i++) {
			LocalDateTime now = LocalDateTime.of(2019, 02, 28, 16, 15 + i);
			session.put("tank1", new Timetag(now, Scale.Hour), "0", i);
		}
		session.close();
		Digester digester = new Digester(new FileDatalake(new File("temp/datalake")), new File("temp/session"));
		digester.push(handler.sessions());
		digester.seal();
		ZetReader reader = new ZetReader(new File("temp/datalake/sets/tank1/2019022816/0.zim"));
		for (int i = 1; i < 30; i++) TestCase.assertEquals(reader.next(), i);
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}
}