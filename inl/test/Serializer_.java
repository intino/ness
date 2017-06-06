import io.intino.ness.inl.Serializer;
import org.junit.Test;
import schemas.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static io.intino.ness.inl.Serializer.serialize;
import static java.text.DateFormat.DEFAULT;
import static java.text.DateFormat.FULL;
import static java.text.DateFormat.LONG;
import static java.util.Arrays.asList;
import static messages.Messages.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Serializer_ {

	@Test
	public void should_serialize_attributes_and_component_of_a_class() throws Exception {
		Person person = new Person("Jose", 50, date(2016, 10, 4, 10, 10, 11), new Country("Spain"));
		assertThat(serialize(person).toInl(), is(MessageWithComponent));
	}

	@Test
	public void should_serialize_array_attributes_of_a_class() throws Exception {
		Menu menu = new Menu(new String[]{"Soup", "Lobster", "Mussels", "Cake"}, new Double[]{5.0, 24.5, 8.0, 7.0}, new Boolean[]{true, false});
		assertThat(serialize(menu).toInl(), is(MenuMessage));
	}

	@Test
	public void should_serialize_empty_array_attributes_of_a_class() throws Exception {
		Menu menu = new Menu(new String[]{}, new Double[]{}, new Boolean[]{true, false});
		assertThat(serialize(menu).toInl(), is(EmptyMenuMessage));
	}

	@Test
	public void should_serialize_array_attribute_with_null_values_of_a_class() throws Exception {
		Menu menu = new Menu(new String[]{"Soup", null, "Mussels", "Cake"}, new Double[]{5.0, null, 8.0, 7.0}, new Boolean[]{true, false});
		assertThat(serialize(menu).toInl(), is(NullValueMenuMessage));
	}

	@Test
	public void should_serialize_a_class_with_mapping() throws Exception {
		MenuX menu = new MenuX(new String[]{"Soup", "Lobster", "Mussels", "Cake"}, new Double[]{5.0, 24.5, 8.0, 7.0}, new Boolean[]{true, false});
		Serializer serializer = serialize(menu).
				map("MenuX", "Menu").
				map("_meals", "meals").
				map("_prices", "prices").
				map("_availability", "availability");
		assertThat(serializer.toInl(), is(MenuMessage));
	}

	@Test
	public void should_not_serialize_null_attributes_of_a_class() throws Exception {
		Person person = new Person("Jose", 50, null, null);
		assertThat(serialize(person).toInl(), is("[Person]\nname: Jose\nmoney: 50.0\n"));
	}

	@Test
	public void should_serialize_message_with_multiple_components() throws Exception {
		Teacher teacher = new Teacher("Jose", 50, date(2016, 10, 4, 20, 10, 11), new Country("Spain"));
		teacher.university = "ULPGC";
		teacher.add(new Phone("+150512101402", new Country("USA")));
		teacher.add(new Phone("+521005101402", new Country("Mexico")));
		assertThat(serialize(teacher).toInl(), is(MultipleComponentMessage));
	}

	@Test
	public void should_serialize_message_with_multi_line() throws Exception {
		Teacher teacher = new Teacher("Jose\nHernandez", 50, date(2016, 10, 4, 20, 10, 11), new Country("Spain"));
		teacher.university = "ULPGC";
		teacher.add(new Phone("+150512101402", new Country("USA")));
		teacher.add(new Phone("+521005101402", new Country("Mexico")));
		assertThat(serialize(teacher).toInl(), is(MultiLineMessage));
	}

	@Test
	public void should_serialize_a_list_of_objects() throws Exception {
		Status status1 = new Status().battery(78.0).cpuUsage(11.95).isPlugged(true).isScreenOn(false).temperature(29.0).created("2017-03-22T12:56:18Z");
		Status status2 = new Status().battery(78.0).cpuUsage(11.95).isPlugged(true).isScreenOn(true).temperature(29.0).created("2017-03-22T12:56:18Z");
		assertThat(Serializer.serialize(asList(status1, status2)).toInl(), is(StatusMessage.replaceAll(" = ", "=")));
	}


	@Test
	public void should_serialize_crash() throws Exception {
		Crash crash = new Crash();
		crash.instant = parse("Tue Mar 21 07:39:00 UTC 2017");
		crash.app = "io.intino.consul";
		crash.deviceId = "b367172b0c6fe726";
		crash.stack = Stack;
		assertThat(Serializer.serialize(crash).toInl(), is(CrashMessage));
	}

	private Date parse(String s) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.ENGLISH);
		return formatter.parse(s);
	}

	private Date date(int y, int m, int d, int h, int mn, int s) {
		return new Date(y - 1900, m - 1, d, h, mn, s);
	}


}
