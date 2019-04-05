package io.intino.ness.datalake;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zim.ZimStream;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Datalake {
	String EventStoreFolder = "events";
	String SetStoreFolder = "sets";
	String StageFolder = "sets";


	EventStore eventStore();

	SetStore setStore();


	interface EventStore {

		Stream<Tank> tanks();

		Tank tank(String name);

		interface Tank {

			String name();

			ZimStream content();

			ZimStream content(Predicate<Timetag> filter);

		}
	}

	interface SetStore {

		Stream<Tank> tanks();

		Tank tank(String name);

		interface Tank {
			String name();

			Stream<Tub> tubs();

			Tub first();

			Tub last();

			Tub on(Timetag tag);

			Stream<Tub> tubs(int count);

			Stream<Tub> tubs(Timetag from, Timetag to);


		}

		interface Tub {
			Timetag timetag();

			Scale scale();

			Index index();

			Set set(String set);

			Stream<Set> sets();

			Stream<Set> sets(Predicate<Set> filter);

		}

		interface Set {
			String name();

			Timetag timetag();

			int size();

			ZetStream content();

			Stream<Variable> variables();

			Variable variable(String name);

		}


		interface Index {
			String find(long id);

			Stream<Entry> entries();
			class Entry {
				public long id;
				public String set;
			}

		}


		class Variable {
			public final String name;
			public final String value;

			Variable(String name, String value) {
				this.name = name;
				this.value = value;
			}
		}

	}

}
