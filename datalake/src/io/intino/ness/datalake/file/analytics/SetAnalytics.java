package io.intino.ness.datalake.file.analytics;

import io.intino.alexandria.Timetag;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.Datalake.SetStore.Set;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;

public class SetAnalytics {
	private Datalake.SetStore setStore;

	public SetAnalytics(Datalake.SetStore setStore) {
		this.setStore = setStore;
	}

	public TankEvolution evolutionOf(String tank, int count) {
		return evolutionOf(setStore.tank(tank), count);
	}

	public TankHistogram histogramOf(String tank, TankHistogram.Axis axis) {
		return histogramOf(setStore.tank(tank), axis);
	}

	private TankEvolution evolutionOf(Datalake.SetStore.Tank tank, int count) {
		return new TankEvolution(tank.tubs(count));
	}

	private TankHistogram histogramOf(Datalake.SetStore.Tank tank, TankHistogram.Axis axis) {
		return new TankHistogram(axis, tank.last());
	}

	public interface Point<T> {
		T item();

		int size();
	}

	public static class TankHistogram<T> {
		private final TankHistogram.Axis<T> axis;
		private final Map<T, Integer> data;
		private final Datalake.SetStore.Tub tub;

		public TankHistogram(TankHistogram.Axis axis, Datalake.SetStore.Tub tub) {
			this.axis = axis;
			this.data = new HashMap<>();
			this.tub = tub;
		}

		public Stream<Point> points() {
			return points(Optional.ofNullable(tub).map(Datalake.SetStore.Tub::sets).orElse(empty()));
		}

		public Stream<Point> points(Predicate<Set> filter) {
			return points(Optional.ofNullable(tub).map(t -> t.sets(filter)).orElse(empty()));
		}

		private Stream<Point> points(Stream<Set> sets) {
			fillDataWith(sets);
			return data.keySet().stream().sorted(axis.sorting()).map(this::pointOf);
		}

		private void fillDataWith(Stream<Set> sets) {
			data.clear();
			sets.forEach(this::put);
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

		public void put(Set set) {
			T item = axis.itemOf(set);
			data.put(item, sizeOf(item) + sizeOf(set));
		}

		private Integer sizeOf(T item) {
			return data.getOrDefault(item, 0);
		}

		private int sizeOf(Set set) {
			return set.size();
		}

		public interface Axis<T> {
			T itemOf(Set set);

			Comparator<? super T> sorting();
		}

	}

	public static class TankEvolution {
		private Stream<Datalake.SetStore.Tub> tubs;

		public TankEvolution(Stream<Datalake.SetStore.Tub> tubs) {
			this.tubs = tubs;
		}

		public Stream<Point> points() {
			return points(s -> true);
		}

		public Stream<Point> points(Predicate<Set> filter) {
			return tubs.map(t -> point(t, filter));
		}

		private Point<Timetag> point(Datalake.SetStore.Tub tub, Predicate<Set> filter) {
			return new Point<Timetag>() {
				@Override
				public Timetag item() {
					return tub.timetag();
				}

				@Override
				public int size() {
					return tub.sets(filter).mapToInt(Set::size).sum();
				}
			};
		}
	}
}
