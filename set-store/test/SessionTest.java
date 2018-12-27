import io.intino.ness.setstore.session.SessionFileReader;
import io.intino.ness.setstore.session.SessionFileWriter;
import io.intino.sezzet.operators.SetStream;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class SessionTest {

	@Test
	public void should_write_and_read_several_records() {
		File file = new File("temp/test.setfs");
		file.getParentFile().mkdirs();
		Instant instant = Instant.now();
		try {
			SessionFileWriter writer = new SessionFileWriter(file, instant, false);
			for (int i = 0; i < 20; i++) writer.add("tank", "set", i);
			for (int i = 0; i < 20; i++) writer.add("tank2", "set2", i);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			SessionFileReader reader = new SessionFileReader(file);
			assertEquals(instant, reader.instant());
			assertEquals(2, reader.chunks().size());
			assertEquals(1, reader.chunks("tank", "set").size());
			SetStream stream = reader.chunks("tank", "set").get(0).stream();
			for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
			assertEquals(1, reader.chunks("tank2", "set2").size());
			stream = reader.chunks("tank2", "set2").get(0).stream();
			for (int i = 0; i < 20; i++) assertEquals((long) i, stream.next());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		new File("temp/test.setfs").delete();
		new File("temp").delete();
	}
}