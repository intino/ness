package io.intino.ness.core;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.zet.ZetStream;
import io.intino.alexandria.zim.ZimStream;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Datalake {

	Connection connection();

	EventStore eventStore();

	SetStore setStore();

	void push(Stream<Blob> stage);

	void seal();

	interface Connection {
		void connect(String... args);

		void disconnect();
	}

	interface EventStore {

		Stream<Tank> tanks();

		Tank tank(String name);

		Reflow reflow(Reflow.Filter filter);

		Subscription subscribe(Tank tank);

		void unsubscribe(Tank tank);

		interface Tank {

			String name();

			ZimStream content();

			ZimStream content(Predicate<Timetag> filter);

		}

		interface Subscription {
			default void using(MessageHandler... messageHandlers) {
				using(null, messageHandlers);
			}

			void using(String clientId, MessageHandler... messageHandlers);
		}

		interface Reflow {
			void next(int blockSize, MessageHandler... messageHandlers);

			interface Filter {
				boolean allow(Tank tank);

				boolean allow(Tank tank, Timetag timetag);
			}
		}

		interface MessageHandler {
			void handle(Message message);
		}

		interface ReflowHandler {
			void onBlock(int reflowedMessages);

			void onFinish(int reflowedMessages);
		}

	}

	interface SetStore {

		Stream<Tank> tanks();

		Tank tank(String name);

		default Analytics analytics() {
			return new Analytics(this);
		}

		interface Tank {
			String name();

			Stream<Tub> tubs();

			Tub first();

			Tub last();

			Tub on(Timetag tag);

			Stream<Tub> tubs(int count);

			Stream<Tub> tubs(Timetag from, Timetag to);

			interface Tub {
				Timetag timetag();

				Scale scale();

				Set set(String set);

				Stream<Set> sets();

				Stream<Set> sets(SetFilter filter);

				interface Set {
					String name();

					Timetag timetag();

					int size();

					ZetStream content();

					Stream<Variable> variables();

					Variable variable(String name);

				}
			}

		}

		interface SetFilter extends Predicate<Tank.Tub.Set> {
		}

		class Variable {
			public String name;
			public String value;

			public Variable(String name, Object value) {
				this.name = name;
				this.value = value.toString();
			}
		}

		class Analytics {
			private SetStore setStore;

			public Analytics(SetStore setStore) {
				this.setStore = setStore;
			}

			public TankEvolution evolutionOf(String tank, int count) {
				return evolutionOf(setStore.tank(tank), count);
			}

			public TankHistogram histogramOf(String tank, TankHistogram.Axis axis) {
				return histogramOf(setStore.tank(tank), axis);
			}

			private TankEvolution evolutionOf(Tank tank, int count) {
				return new TankEvolution(tank.tubs(count));
			}

			private TankHistogram histogramOf(Tank tank, TankHistogram.Axis axis) {
				return new TankHistogram(axis, tank.last());
			}

			public interface Point<T> {
				T item();

				int size();
			}

			public static class TankHistogram<T> {
				private final Axis<T> axis;
				private final Map<T, Integer> data;

				public TankHistogram(Axis axis, SetStore.Tank.Tub tub) {
					this.axis = axis;
					this.data = new HashMap<>();
					this.initData(tub);
				}

				public Stream<Point> points() {
					return data.keySet().stream().sorted(axis.sorting()).map(this::pointOf);
				}

				private void initData(SetStore.Tank.Tub tub) {
					tub.sets().forEach(this::put);
				}

				private Point<T> pointOf(T item) {
					return new Point<T>() {
						@Override
						public T item() {
							return item;
						}

						@Override
						public int size() {
							return sizeOf(item);
						}
					};
				}

				public void put(SetStore.Tank.Tub.Set set) {
					T item = axis.itemOf(set);
					data.put(item, sizeOf(item) + sizeOf(set));
				}

				private Integer sizeOf(T item) {
					return data.getOrDefault(item, 0);
				}

				private int sizeOf(SetStore.Tank.Tub.Set set) {
					return set.size();
				}

				public interface Axis<T> {
					T itemOf(SetStore.Tank.Tub.Set set);

					Comparator<? super T> sorting();
				}

			}

			public static class TankEvolution {
				private Stream<Tank.Tub> tubs;

				public TankEvolution(Stream<Tank.Tub> tubs) {
					this.tubs = tubs;
				}

				public Stream<Point> points() {
					return points(s -> true);
				}

				public Stream<Point> points(SetStore.SetFilter filter) {
					return tubs.map(t -> point(t, filter));
				}

				private Point<Timetag> point(SetStore.Tank.Tub tub, SetStore.SetFilter filter) {
					return new Point<Timetag>() {
						@Override
						public Timetag item() {
							return tub.timetag();
						}

						@Override
						public int size() {
							return tub.sets(filter).mapToInt(SetStore.Tank.Tub.Set::size).sum();
						}
					};
				}
			}
		}

	}

}
