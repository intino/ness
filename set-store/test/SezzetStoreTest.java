import io.intino.sezzet.SezzetStore;
import io.intino.sezzet.model.graph.rules.Scale;
import org.junit.Test;

import java.io.File;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SezzetStoreTest {

	@Test
	public void should_get_all_tanks_properly() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		assertEquals(2, store.tanks().size());
		assertEquals("tank1", store.tanks().get(0));
		assertEquals("tank2", store.tanks().get(1));
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		assertEquals(2, store.setsOf("tank1", from).size());
		assertEquals("set1", store.setsOf("tank1", from).get(0));
		assertEquals("set2", store.setsOf("tank1", from).get(1));
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		assertEquals(3, store.setsOf("tank1", from, to).size());
		assertEquals("set1", store.setsOf("tank1", from, to).get(0));
		assertEquals("set2", store.setsOf("tank1", from, to).get(1));
		assertEquals("set3", store.setsOf("tank1", from, to).get(2));
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range_and_a_regex() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		assertEquals(3, store.setsOf("tank1", "set*", from, to).size());
		assertEquals("set1", store.setsOf("tank1", from, to).get(0));
		assertEquals("set2", store.setsOf("tank1", from, to).get(1));
		assertEquals("set3", store.setsOf("tank1", from, to).get(2));
	}

	@Test
	public void should_get_the_folders_for_a_tank_in_the_period() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		assertEquals(2, store.foldersOf("tank1", from, to).size());
		assertEquals("test-res/tank1/201809", store.foldersOf("tank1", from, to).get(0).getPath().replace("\\", "/"));
		assertEquals("test-res/tank1/201810", store.foldersOf("tank1", from, to).get(1).getPath().replace("\\", "/"));
	}

	@Test
	public void should_get_the_files_for_a_tank_and_a_set_in_the_period() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		assertEquals(2, store.filesOf("tank1", "set1", from, to).size());
		assertEquals("test-res/tank1/201809/set1.set", store.filesOf("tank1", "set1", from, to).get(0).getPath().replace("\\", "/"));
		assertEquals("test-res/tank1/201810/set1.set", store.filesOf("tank1", "set1", from, to).get(1).getPath().replace("\\", "/"));
	}

	@Test
	public void should_write_and_read_variables_on_a_set() {
		SezzetStore store = new SezzetStore(new File("test-res"), Scale.Month);
		Instant instant = Instant.parse("2018-10-01T00:00:00Z");
		assertNull(store.valueOf("tank1", "set3", "var1", instant));
		store.append("tank1", "set3", "var1", "value1", instant);
		assertEquals("value1", store.valueOf("tank1", "set3", "var1", instant));
		store.append("tank1", "set3", "var1", "value2", instant);
		assertEquals("value2", store.valueOf("tank1", "set3", "var1", instant));
		new File(store.folderOf("tank1", instant), "tank1.info").delete();
	}


}