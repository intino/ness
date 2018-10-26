package io.intino.ness.setstore;


import io.intino.ness.setstore.SetStore.Tank.Tub.Set;
import io.intino.ness.setstore.session.SessionFileWriter;
import io.intino.sezzet.operators.SetStream;

import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

public interface SetStore {

	Scale scale();

	List<Tank> tanks();

	Tank tank(String name);

	SessionFileWriter createSession(Instant instant);

	//void commitSession(Session session) TODO

	File storeSegment(Instant instant, String segment, SetStream stream);//TODO remove?

	void seal(); //TODO hace falta?

	interface Tank {
		String name();

		Tub tub(Instant instant);

		List<Tub> tubs(Instant from, Instant to);

		List<Set> setsOf(Instant from, Instant to);

		List<Set> setsOf(Instant from, Instant to, SetFilter filter);

		interface Tub {
			String name();

			Tank tank();

			Instant instant();

			Set set(String set);

			List<Set> sets();

			List<Set> sets(SetFilter filter);

			interface Set {
				String name();

				Tub tub();

				SetStream content();

				List<Variable> variables();

				Variable variable(String name);

				void define(Variable variable);

				void append(long... ids);

				void append(InputStream stream);
			}
		}
	}

	interface SetFilter extends Predicate<Set> {

	}

	class Variable {
		public String name;
		public String value;

		public Variable(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
}
