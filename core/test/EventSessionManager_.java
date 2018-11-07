import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.ness.core.Scale;
import io.intino.ness.core.Timetag;
import io.intino.ness.core.sessions.Fingerprint;
import io.intino.ness.core.sessions.SetSessionFileReader;
import io.intino.ness.core.sessions.SetSessionFileWriter;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

import static io.intino.ness.core.fs.FSEventStore.EventExtension;
import static org.junit.Assert.*;

@Ignore
public class EventSessionManager_ {

	private static void deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) for (File file : allContents) deleteDirectory(file);
		directoryToBeDeleted.delete();
	}

	@Test
	public void should_create_an_event_session_and_seal_it() {
		createSessionAndSeal();
		Instant instant = Instant.parse("2018-09-10T00:00:00Z");
		File file = new File("temp/events/tank1/201809" + EventExtension);
		assertTrue(file.exists());
		ZimReader zimReader = new ZimReader(file);
		assertEquals(message(instant, 0).toString(), zimReader.next().toString());
		assertFalse(zimReader.hasNext());
		zimReader.close();
	}

	@Test
	public void should_create_an_event_session_and_merge_when_sealing_it() throws IOException {
		ZimWriter writer = new ZimWriter(new File("temp/events/tank1/201809.zim"));
		Instant instant = Instant.parse("2018-09-09T00:00:00Z");
		writer.write(message(instant, -1));
		writer.close();
		createSessionAndSeal();
		File file = new File("temp/events/tank1/201809" + EventExtension);
		assertTrue(file.exists());
		ZimReader zimReader = new ZimReader(file);
		assertEquals(message(instant, -1).toString(), zimReader.next().toString());
		assertEquals(message(Instant.parse("2018-09-10T00:00:00Z"), 0).toString(), zimReader.next().toString());
		assertFalse(zimReader.hasNext());
		zimReader.close();
	}

	private void createSessionAndSeal() {
//		FSStage stage = new FSStage(new File("temp"));
//		EventSession session = stage.createEventSession();
//		Timetag timetag = new Timetag("201809");
//		Instant instant = Instant.parse("2018-09-10T00:00:00Z");
//		session.put("tank1", timetag, message(instant, 0));
//		session.close();
//		FSDatalake datalake = new FSDatalake(new File("temp"));
//		datalake.push(stage.blobs());
//		datalake.seal();
	}

	private Message message(Instant instant, int index) {
		return new Message("tank1").set("ts", instant.toString()).set("index", index);
	}

//	@Test TODO revisar
//	public void create_session_and_seal_it() {
//		FSStage stage = new FSStage(new File("temp"));
//		SetSession session = stage.createSetSession();
//		Timetag timetag = new Timetag("201809");
//		for (int i = 0; i < 20; i++) session.put("tank1", timetag, "set1", i);
//		for (int i = 0; i < 20; i++) session.put("tank1", timetag, "set2", i);
//		for (int i = 0; i < 20; i++) session.put("tank2", timetag, "set1", i);
//		for (int i = 0; i < 20; i++) session.put("tank2", timetag, "set2", i);
//		session.close();
//
//		FSDatalake datalake = new FSDatalake(new File("temp"));
//		datalake.push(stage);
//		datalake.seal();
//		assertTrue(new File("temp/sets/tank1/201809/set1.zet").exists());
//		assertTrue(new File("temp/sets/tank1/201809/set2.zet").exists());
//		assertTrue(new File("temp/sets/tank2/201809/set1.zet").exists());
//		assertTrue(new File("temp/sets/tank2/201809/set2.zet").exists());
//		assertEquals(8 * 20, new File("temp/sets/tank1/201809/set1.zet").length());
//		assertEquals(8 * 20, new File("temp/sets/tank1/201809/set2.zet").length());
//		assertEquals(8 * 20, new File("temp/sets/tank2/201809/set1.zet").length());
//		assertEquals(8 * 20, new File("temp/sets/tank2/201809/set2.zet").length());
//	}

	@Test
	public void should_write_and_read_several_records() throws IOException {
		File file = new File("temp/test.setfs");
		file.getParentFile().mkdirs();
		Timetag timetag = new Timetag(LocalDateTime.now(), Scale.Month);
		SetSessionFileWriter writer = new SetSessionFileWriter(new FileOutputStream(file));
		for (int i = 0; i < 20; i++) writer.add("tank", timetag, "set", i);
		for (int i = 0; i < 20; i++) writer.add("tank2", timetag, "set2", i);
		writer.close();

		SetSessionFileReader reader = new SetSessionFileReader(file);
		assertEquals(2, reader.chunks().size());
		assertEquals(1, reader.chunks(Fingerprint.of("tank", timetag, "set")).size());
		ZetStream stream = reader.chunks(Fingerprint.of("tank", timetag, "set")).get(0).stream();
		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());
		assertEquals(1, reader.chunks(Fingerprint.of("tank2", timetag, "set2")).size());
		stream = reader.chunks(Fingerprint.of("tank2", timetag, "set2")).get(0).stream();
		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());

	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

}