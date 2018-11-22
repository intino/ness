import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake.SetStore.Tank;
import io.intino.ness.core.Datalake.SetStore.Tank.Tub.Set;
import io.intino.ness.core.fs.FSSetStore;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FSSetStoreTest {

	@Test
	public void should_get_all_tanks_properly() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		assertEquals(2, store.tanks().count());
		assertEquals("tank1", store.tanks().toArray(Tank[]::new)[0].name());
		assertEquals("tank2", store.tanks().toArray(Tank[]::new)[1].name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		List<Set> sets = store.tank("tank1").on(new Timetag("201809")).sets().collect(toList());
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		Timetag from = new Timetag("201809");
		Timetag to = new Timetag("201810");
		Tank tank = store.tank("tank1");
		List<Set> sets = tank.tubs(from, to).flatMap(Tank.Tub::sets).collect(toList());
		assertEquals(4, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals("set2", sets.get(1).name());
		assertEquals(from, sets.get(1).timetag());
		assertEquals("set1", sets.get(2).name());
		assertEquals(to, sets.get(2).timetag());
		assertEquals("set3", sets.get(3).name());
	}

	@Test
	public void should_get_the_sets_of_a_tank_for_a_given_time_range_and_a_regex() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		Timetag from = new Timetag("201809");
		Timetag to = new Timetag("201810");
		Tank tank = store.tank("tank1");
		assertEquals(4, tank.tubs(from, to).flatMap(Tank.Tub::sets).filter(t -> t.name().matches("set.*")).count());
	}

	@Test
	public void should_get_the_folders_for_a_tank_in_the_period() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		Timetag from = new Timetag("201809");
		Timetag to = new Timetag("201810");
		Tank tank = store.tank("tank1");
		List<Tank.Tub> tubs = tank.tubs(from, to).collect(toList());
		assertEquals(2, tubs.size());
		assertEquals(from.toString(), tubs.get(0).timetag().toString());
		assertEquals(to.toString(), tubs.get(1).timetag().toString());
		assertEquals("201809", tubs.get(0).timetag().toString());
		assertEquals("201810", tubs.get(1).timetag().toString());
	}

	@Test
	public void should_get_the_files_for_a_tank_and_a_set_in_the_period() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		Timetag from = new Timetag("201809");
		Timetag to = new Timetag("201810");
		Tank tank = store.tank("tank1");
		List<Set> sets = tank.tubs(from, to).flatMap(Tank.Tub::sets).filter(s -> s.name().equals("set1")).collect(toList());
		assertEquals(2, sets.size());
		assertEquals("set1", sets.get(0).name());
		assertEquals(from, sets.get(0).timetag());
		assertEquals("set1", sets.get(1).name());
		assertEquals(to, sets.get(1).timetag());
	}

	@Test
	public void should_write_and_read_variables_on_a_set() {
		FSSetStore store = new FSSetStore(new File("test-res/sets"));
		Timetag instant = new Timetag("201810");
		Tank.Tub tub = store.tank("tank1").on(instant);
		Set set3 = tub.set("set3");
		assertNotNull(set3);
		assertEquals("value1", set3.variable("var1").value);
	}

	private void clean() {
		new File("test-res/tank1/201810/" + FSSetStore.MetadataFilename).delete();
	}

}