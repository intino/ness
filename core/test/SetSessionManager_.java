import org.junit.Ignore;

@Ignore //TODO revisar
public class SetSessionManager_ {
//
//	private static boolean deleteDirectory(File directoryToBeDeleted) {
//		File[] allContents = directoryToBeDeleted.listFiles();
//		if (allContents != null) {
//			for (File file : allContents) {
//				deleteDirectory(file);
//			}
//		}
//		return directoryToBeDeleted.delete();
//	}
//
//	@Test
//	public void should_define_variable() {
//		FSStage stage = new FSStage(new File("temp"));
//		SetSession session = stage.createSetSession();
//		Timetag timetag = new Timetag("201809");
//		session.define("tank1", timetag, "set1", new Variable("size", 10));
//		session.close();
//
//		FSDatalake datalake = new FSDatalake(new File("temp"));
//		datalake.push(stage);
//		datalake.seal();
//		assertTrue(new File("temp/sets/tank1/201809/" + MetadataFilename).exists());
//		assertEquals(1, datalake.setStore().tank("tank1").on(timetag).set("set1").variables().count());
//		assertEquals("10", datalake.setStore().tank("tank1").on(timetag).set("set1").variable("size").value);
//	}
//
//	@Test
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
//
//	@Test
//	public void should_write_and_read_several_records() throws IOException {
//		File file = new File("temp/test.setfs");
//		file.getParentFile().mkdirs();
//		Timetag timetag = new Timetag(LocalDateTime.now(), Scale.Month);
//		SetSessionFileWriter writer = new SetSessionFileWriter(new FileOutputStream(file));
//		for (int i = 0; i < 20; i++) writer.add("tank", timetag, "set", i);
//		for (int i = 0; i < 20; i++) writer.add("tank2", timetag, "set2", i);
//		writer.close();
//
//		SetSessionFileReader reader = new SetSessionFileReader(file);
//		assertEquals(2, reader.chunks().size());
//		assertEquals(1, reader.chunks(Fingerprint.of("tank", timetag, "set")).size());
//		ZetStream stream = reader.chunks(Fingerprint.of("tank", timetag, "set")).get(0).stream();
//		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
//		assertFalse(stream.hasNext());
//		assertEquals(1, reader.chunks(Fingerprint.of("tank2", timetag, "set2")).size());
//		stream = reader.chunks(Fingerprint.of("tank2", timetag, "set2")).get(0).stream();
//		for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
//		assertFalse(stream.hasNext());
//
//	}
//
//	@After
//	public void tearDown() {
//		deleteDirectory(new File("temp"));
//	}

}