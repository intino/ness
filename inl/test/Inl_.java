import io.intino.ness.Inl;
import io.intino.ness.inl.Message;
import org.junit.Test;
import schemas.AlertModified;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Inl_ {

	private String text = "[Person]\nname: Mike\ncountry: USA\n";

	@Test
	public void should_load() throws Exception {
		List<Message> messages = Inl.load(text);
		assertThat(messages.size(), is(1));
		assertThat(messages.get(0).type(), is("Person"));
		assertThat(messages.get(0).read("name"), is("Mike"));
		assertThat(messages.get(0).read("country"), is("USA"));

	}

	@Test
	public void should_serialize() throws Exception {
		assertThat(Inl.serialize(new Person("Mike", "USA")), is(text));
	}

	@Test
	public void should_deserialize() throws Exception {
		Person person = Inl.deserialize(text).next(Person.class);
		assertThat(person.name, is("Mike"));
		assertThat(person.country, is("USA"));
	}

	@Test
	public void should_deserialize_all() throws Exception {
		List<Person> people = Inl.deserializeAll(text, Person.class);
		assertThat(people.size(), is(1));
		assertThat(people.get(0).name, is("Mike"));
		assertThat(people.get(0).country, is("USA"));
	}

	@Test
	public void should_throw_exception_when_attribute_does_not_exist() throws Exception {
		String text = "[Person]\nname: Mike\ncountry: USA\ninstant: 2017-04-17T16:01:41.716Z\n";
		try {
			Inl.deserialize(text).next(Person.class);
			assertThat("Exception not thrown", false);
		} catch (Exception e) {
			assertThat("Exception thrown", true);
		}
	}

	@Test
	public void should_serialize_schema() throws Exception {
		Inl.serialize(new AlertModified().alert("Alerts#bbc15556-244b-45af-97b9-c0f18b1e42be").active(true).mailingList(asList("cambullonero@monentia.es")).applyToAllStations(false));
	}


	public static class Person {
		private String name;
		private String country;

		public Person() {
		}

		public Person(String name, String country) {
			this.name = name;
			this.country = country;
		}
	}
}
