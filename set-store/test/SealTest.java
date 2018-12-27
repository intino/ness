import org.junit.After;
import org.junit.Test;

import java.io.File;

public class SealTest {

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
	public void create_session_and_seal_it() {
//		try {
//			Instant instant = Instant.parse("2018-09-01T00:00:00Z");
//			SetStore store = new FileSetStore(new File("temp"), Scale.Month);
//			SessionFileWriter session = store.createSession(instant);
//			for (int i = 0; i < 20; i++) session.add("tank1", "set1", i);
//			for (int i = 0; i < 20; i++) session.add("tank1", "set2", i);
//			for (int i = 0; i < 20; i++) session.add("tank2", "set1", i);
//			for (int i = 0; i < 20; i++) session.add("tank2", "set2", i);
//			session.close();
//			store.seal();
//			assertTrue(new File("temp/tank1/201809/set1.set").exists());
//			assertTrue(new File("temp/tank1/201809/set2.set").exists());
//			assertTrue(new File("temp/tank2/201809/set1.set").exists());
//			assertTrue(new File("temp/tank2/201809/set2.set").exists());
//			assertEquals(8 * 20, new File("temp/tank1/201809/set1.set").length());
//			assertEquals(8 * 20, new File("temp/tank1/201809/set2.set").length());
//			assertEquals(8 * 20, new File("temp/tank2/201809/set1.set").length());
//			assertEquals(8 * 20, new File("temp/tank2/201809/set2.set").length());
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		}
	}

	@After
	public void tearDown() {
		deleteDirectory(new File("temp"));
	}
}