package io.intino.ness.setstore;

import io.intino.ness.setstore.SetStore.Tank.Tub.Set;
import io.intino.sezzet.operators.SetStream;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface SetStore {
	Scale scale();

	List<Tank> tanks();

	Tank tank(String name);

	File storeSegment(String segment, Timetag tag, SetStream stream);

	interface Tank {
		String name();

		List<Tub> tubs();

		Tub first();

		Tub last();

		Tub on(Timetag tag);

		Stream<Tub> tubs(int count);

		List<Tub> tubs(Timetag from, Timetag to);

		List<Set> setsOf(Timetag from, Timetag to);

		List<Set> setsOf(Timetag from, Timetag to, SetFilter filter);

		interface Tub {
			String name();

			Tank tank();

			Timetag timetag();

			Set set(String set);

			List<Set> sets();

			List<Set> sets(SetFilter filter);

			interface Set {
				String name();

				int size();

				Tub tub();

				SetStream content();

				List<Variable> variables();

				Variable variable(String name);

				void put(long... ids);

				void put(List<Long> stream);

				void define(Variable variable);
			}
		}
	}

	class Variable {
		public String name;
		public String value;

		public Variable(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	class Timetag {
		private final Instant instant;
		private final Scale scale;
		private final String tag;

		public Timetag(Instant instant, Scale scale) {
			this.instant = instant;
			this.scale = scale;
			this.tag = scale.tag(instant);
		}

		public String value() {
			return tag;
		}

		Timetag next() {
			return new Timetag(scale.plus(instant), scale);
		}

		Timetag before() {
			return new Timetag(scale.minus(instant), scale);
		}

		Instant toInstant() {
			return instant;
		}

		@Override
		public String toString() {
			return tag;
		}
	}

	interface SetFilter extends Predicate<Set> {
	}
}