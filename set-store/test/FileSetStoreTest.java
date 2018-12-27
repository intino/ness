import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.SetStore.Timetag;
import io.intino.ness.setstore.file.FileSetStore;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static io.intino.ness.setstore.Scale.Month;
import static java.time.Instant.parse;
import static org.junit.Assert.*;

public class FileSetStoreTest {

	@Test
	public void should_get_all_tanks_properly() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		assertEquals(2, store.tanks().size());
		assertEquals("tank1", store.tanks().get(0).name());
		assertEquals("tank2", store.tanks().get(1).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag from = new Timetag(parse("2018-09-01T00:00:00Z"), Month);
		List<SetStore.Tank.Tub.Set> sets = store.tank("tank1").on(from).sets();
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag from = new Timetag(parse("2018-09-01T00:00:00Z"), Month);
		Timetag to = new Timetag(parse("2018-10-01T00:00:00Z"), Month);
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub.Set> sets = tank.setsOf(from, to);
		assertEquals(4, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
		Timetag timetag = sets.get(1).tub().timetag();
		assertEquals(from.toString(), timetag.toString());
		assertEquals("set1", sets.get(2).name());
		assertEquals(to.toString(), sets.get(2).tub().timetag().toString());
		assertEquals("set3", sets.get(3).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range_and_a_regex() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag from = new Timetag(parse("2018-09-01T00:00:00Z"), Month);
		Timetag to = new Timetag(parse("2018-10-01T00:00:00Z"), Month);
		SetStore.Tank tank = store.tank("tank1");
		assertEquals(4, tank.setsOf(from, to, t -> t.name().matches("set.*")).size());
	}

	@Test
	public void should_get_the_folders_for_a_tank_in_the_period() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag from = new Timetag(Instant.parse("2018-09-01T00:00:00Z"), Scale.Month);
		Timetag to = new Timetag(Instant.parse("2018-10-01T00:00:00Z"), Scale.Month);
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub> tubs = tank.tubs(from, to);
		assertEquals(2, tubs.size());
		assertEquals(from.toString(), tubs.get(0).timetag().toString());
		assertEquals(to.toString(), tubs.get(1).timetag().toString());
		assertEquals("201809", tubs.get(0).timetag().toString());
		assertEquals("201810", tubs.get(1).timetag().toString());
	}

	@Test
	public void should_get_the_files_for_a_tank_and_a_set_in_the_period() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag from = new Timetag(Instant.parse("2018-09-01T00:00:00Z"), Scale.Month);
		Timetag to = new Timetag(Instant.parse("2018-10-01T00:00:00Z"), Scale.Month);
		SetStore.Tank tank = store.tank("tank1");
		List<SetStore.Tank.Tub.Set> sets = tank.setsOf(from, to, s -> s.name().equals("set1"));
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals(from.toString(), sets.get(0).tub().timetag().toString());
		assertEquals("set1", sets.get(1).name());
		assertEquals(to.toString(), sets.get(1).tub().timetag().toString());
	}

	@Test
	public void should_write_and_read_variables_on_a_set() {
		FileSetStore store = new FileSetStore(new File("test-res"), Month);
		Timetag instant = new Timetag(Instant.parse("2018-10-01T00:00:00Z"), Scale.Month);;
		SetStore.Tank.Tub tub = store.tank("tank1").on(instant);
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