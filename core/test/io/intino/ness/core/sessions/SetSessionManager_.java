package io.intino.ness.core.sessions;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.core.Blob;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.fs.FS;
import io.intino.ness.core.fs.FSDatalake;
import org.junit.After;
import org.junit.Test;

import java.io.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static io.intino.ness.core.fs.FSSetStore.MetadataFilename;
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
		Datalake datalake = new FSDatalake(new File("temp"));
		datalake.push(blobs());
		datalake.seal();
		assertTrue(new File("temp/sets/tank1/201809/" + MetadataFilename).exists());
		assertEquals(2, datalake.setStore().tank("tank1").on(timetag).set("set1").variables().count());
		assertEquals("10", datalake.setStore().tank("tank1").on(timetag).set("set1").variable("var").value);

		assertTrue(new File("temp/sets/tank1/201809/set1.zet").exists());
		assertTrue(new File("temp/sets/tank1/201809/set2.zet").exists());
		assertTrue(new File("temp/sets/tank2/201809/set1.zet").exists());
		assertTrue(new File("temp/sets/tank2/201809/set2.zet").exists());
		assertEquals(0, FS.filesIn(new File("temp/stage/"), f -> f.getName().endsWith(".blob")).count());
	}

	private Stream<Blob> blobs() {
		return FS.filesIn(new File("test-res/setstage"), f -> f.getName().endsWith(".blob"))
				.map(file -> new Blob() {
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
		File file = new File("temp/test.setfs");
		file.getParentFile().mkdirs();
		Timetag timetag = new Timetag(LocalDateTime.now(), Scale.Month);
		SetSessionFileWriter writer = new SetSessionFileWriter(new GZIPOutputStream(new FileOutputStream(file)));
		for (int i = 0; i < 20; i++) writer.add("tank", timetag, "set", i);
		for (int i = 0; i < 20; i++) writer.add("sets/tank2", timetag, "set2", i);
		writer.close();

		SetSessionFileReader reader = new SetSessionFileReader(file);
		assertEquals(2, reader.chunks().count());
		assertEquals(1, reader.chunks(Fingerprint.of("tank", timetag, "set")).count());
		ZetStream stream = reader.chunks(Fingerprint.of("tank", timetag, "set")).findFirst().get().stream();
		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());
		assertEquals(1, reader.chunks(Fingerprint.of("sets/tank2", timetag, "set2")).count());
		stream = reader.chunks(Fingerprint.of("sets/tank2", timetag, "set2")).findFirst().get().stream();
		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		assertFalse(stream.hasNext());

	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}

}