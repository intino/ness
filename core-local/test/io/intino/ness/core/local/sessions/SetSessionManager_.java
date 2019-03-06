package io.intino.ness.core.local.sessions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Session;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.SetSessionFileWriter;
import io.intino.ness.core.local.FS;
import io.intino.ness.core.local.LocalDatalake;
import io.intino.ness.core.local.LocalSetStore;
import io.intino.ness.core.sessions.Fingerprint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
	public void should_define_variable() {
		deleteDirectory(new File("temp"));
		Timetag timetag = new Timetag("201809");
		Datalake datalake = new LocalDatalake(new File("temp"));
		datalake.push(sessions());
		datalake.seal();
		assertTrue(new File("temp/sets/tank1/201809/" + LocalSetStore.MetadataFilename).exists());
		assertEquals(1, datalake.setStore().tank("tank1").on(timetag).set("set1").variables().count());
		assertEquals("10", datalake.setStore().tank("tank1").on(timetag).set("set1").variable("var").value);

		assertTrue(new File("temp/sets/tank1/201809/set1.zet").exists());
		assertTrue(new File("temp/sets/tank1/201809/set2.zet").exists());
		assertTrue(new File("temp/sets/tank2/201809/set3.zet").exists());
		Assert.assertEquals(0, FS.filesIn(new File("temp/stage/"), f -> f.getName().endsWith(".blob")).count());
	}

	private Stream<Session> sessions() {
		return FS.filesIn(new File("test-res/setstage"), f -> f.getName().endsWith(".blob"))
				.map(file -> new Session() {
					@Override
					public String name() {
						String name = file.getName();
						return name.substring(0, name.lastIndexOf("."));
					}

					@Override
					public Type type() {
						return file.getName().contains("Metadata") ? Type.setMetadata : Type.set;
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

	@Test
	public void should_write_and_read_several_records() throws IOException {
		File file = new File("temp/test.blob");
		file.getParentFile().mkdirs();
		Timetag timetag = new Timetag("201809");
		SetSessionFileWriter writer = new SetSessionFileWriter(new FileOutputStream(file));
		for (int i = 0; i < 20; i++) writer.add("tank1", timetag, "set1", i);
		for (int i = 0; i < 20; i++) writer.add("tank1", timetag, "set2", i);
		for (int i = 0; i < 30; i++) writer.add("tank2", timetag, "set3", i);
		writer.close();

		SetSessionFileReader reader = new SetSessionFileReader(file);
		assertEquals(3, reader.fingerprints().size());
		assertEquals(1, reader.streamsOf(Fingerprint.of("tank1", timetag, "set1")).size());
		ZetStream stream = reader.streamsOf(Fingerprint.of("tank1", timetag, "set1")).get(0);
		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());
		assertEquals(1, reader.streamsOf(Fingerprint.of("tank2", timetag, "set3")).size());
		stream = reader.streamsOf(Fingerprint.of("tank2", timetag, "set3")).get(0);
		for (int i = 0; i < 30; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());

	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}
}