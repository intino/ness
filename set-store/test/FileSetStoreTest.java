import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.file.FileSetStore;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.*;

public class FileSetStoreTest {

	@Test
	public void should_get_all_tanks_properly() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		assertEquals(2, store.tanks().size());
		assertEquals("tank1", store.tanks().get(0).name());
		assertEquals("tank2", store.tanks().get(1).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		List<SetStore.Tank.Tub.Set> sets = store.tank("tank1").tub(from).sets();
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub.Set> sets = tank.setsOf(from, to);
		assertEquals(4, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
		assertEquals(from, sets.get(1).tub().instant());
		assertEquals("set1", sets.get(2).name());
		assertEquals(to, sets.get(2).tub().instant());
		assertEquals("set3", sets.get(3).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range_and_a_regex() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		SetStore.Tank tank = store.tank("tank1");
		assertEquals(4, tank.setsOf(from, to, t -> t.name().matches("set.*")).size());
	}

	@Test
	public void should_get_the_folders_for_a_tank_in_the_period() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub> tubs = tank.tubs(from, to);
		assertEquals(2, tubs.size());
		assertEquals(from, tubs.get(0).instant());
		assertEquals(to, tubs.get(1).instant());
		assertEquals("201809", store.scale().tag(tubs.get(0).instant()));
		assertEquals("201810", store.scale().tag(tubs.get(1).instant()));
	}

	@Test
	public void should_get_the_files_for_a_tank_and_a_set_in_the_period() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant from = Instant.parse("2018-09-01T00:00:00Z");
		Instant to = Instant.parse("2018-10-01T00:00:00Z");
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub.Set> sets = tank.setsOf(from, to, s -> s.name().equals("set1"));
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals(from, sets.get(0).tub().instant());
		assertEquals("set1", sets.get(1).name());
		assertEquals(to, sets.get(1).tub().instant());
	}

	@Test
	public void should_write_and_read_variables_on_a_set() {
		FileSetStore store = new FileSetStore(new File("test-res"), Scale.Month);
		Instant instant = Instant.parse("2018-10-01T00:00:00Z");
		SetStore.Tank.Tub tub = store.tank("tank1").tub(instant);
		SetStore.Tank.Tub.Set set3 = tub.set("set3");
		assertNotNull(set3);
		assertNull(set3.variable("var1"));
		set3.define(new SetStore.Variable("var1", "value1"));
		assertEquals("value1", set3.variable("var1").value);
		set3.define(new SetStore.Variable("var1", "value2"));
		assertEquals("value2", set3.variable("var1").value);
		assertTrue(new File("test-res/tank1/201810/tank1.info").delete());
	}

}