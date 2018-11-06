package io.intino.ness.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import static java.lang.Integer.parseInt;

public class Timetag {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
	private final String tag;

	public Timetag(LocalDateTime datetime, Scale scale) {
		this.tag = formatter.format(datetime).substring(0, sizeOf(scale));
	}

	public Timetag(String tag) {
		this.tag = tag;
	}

	public static Timetag of(String tag) {
		return new Timetag(tag);
	}

	public static Timetag of(LocalDateTime datetime, Scale scale) {
		return new Timetag(datetime, scale);
	}

	public LocalDateTime datetime() {
		return new Parser().parse();
	}

	public Scale scale() {
		return Scale.values()[precision()];
	}

	private int precision() {
		return (tag.length() - 4) / 2;
	}

	public String value() {
		return tag;
	}

	public String label() {
		String result = tag;
		for (int i = result.length(); i > 4; i -= 2)
			result = result.substring(0, i - 2) + "-" + result.substring(i - 2);
		return result;
	}

	public Timetag next() {
		return new Timetag(calculate(+1), scale());
	}

	public Timetag previous() {
		return new Timetag(calculate(-1), scale());
	}

	public Iterable<Timetag> iterateTo(Timetag to) {
		return () -> new Iterator<Timetag>() {
			Timetag current = Timetag.this;

			@Override
			public boolean hasNext() {
				return !current.isAfter(to);
			}

			@Override
			public Timetag next() {
				Timetag result = current;
				current = current.next();
				return result;
			}
		};
	}

	private boolean isAfter(Timetag timetag) {
		return datetime().isAfter(timetag.datetime());
	}

	private boolean isBefore(Timetag timetag) {
		return datetime().isBefore(timetag.datetime());
	}

	private LocalDateTime calculate(int amount) {
		return scale().temporalUnit().addTo(datetime(), amount);
	}


	private int sizeOf(Scale scale) {
		return scale.ordinal() * 2 + 4;
	}

	@Override
	public boolean equals(Object o) {
		return tag.equals(((Timetag) o).tag);
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	@Override
	public String toString() {
		return tag;
	}

	public class Parser {

		private final int precision;

		public Parser() {
			this.precision = precision();
		}

		private LocalDateTime parse() {
			return LocalDateTime.of(year(), month(), day(), hour(), minute());
		}

		private int year() {
			return parseInt(tag.substring(0, 4));
		}

		private int month() {
			return hasMonth() ? parseInt((tag).substring(4, 6)) : 1;
		}

		private int day() {
			return hasDay() ? parseInt((tag).substring(6, 8)) : 1;
		}

		private int hour() {
			return hasHour() ? parseInt((tag).substring(8, 10)) : 0;
		}

		private int minute() {
			return hasMinute() ? parseInt((tag).substring(10, 12)) : 0;
		}

		private boolean hasMonth() {
			return precision > 0;
		}

		private boolean hasDay() {
			return precision > 1;
		}

		private boolean hasHour() {
			return precision > 2;
		}

		private boolean hasMinute() {
			return precision > 3;
		}

	}

}
