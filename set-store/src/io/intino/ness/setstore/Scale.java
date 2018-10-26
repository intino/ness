package io.intino.ness.setstore;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public enum Scale {
	Year {
		@Override
		public String tag(Instant instant) {
			return toUtc(instant).getYear() + "";
		}

		@Override
		public String label(Instant instant) {
			return tag(instant);
		}

		@Override
		public Instant plus(Instant instant) {
			return toUtc(instant).plusYears(1).toInstant(ZoneOffset.UTC);
		}

		@Override
		public Instant minus(Instant instant) {
			return toUtc(instant).minusYears(1).toInstant(ZoneOffset.UTC);
		}
	},
	Month {
		@Override
		public String tag(Instant instant) {
			return Year.tag(instant) + String.format("%02d", toUtc(instant).getMonth().getValue());
		}

		@Override
		public String label(Instant instant) {
			return Year.tag(instant) + "-" + String.format("%02d", toUtc(instant).getMonth().getValue());
		}

		@Override
		public Instant plus(Instant instant) {
			return toUtc(instant).plusMonths(1).toInstant(ZoneOffset.UTC);
		}

		@Override
		public Instant minus(Instant instant) {
			return toUtc(instant).minusMonths(1).toInstant(ZoneOffset.UTC);
		}
	},
	Day {
		@Override
		public String tag(Instant instant) {
			return Month.tag(instant) + String.format("%02d", toUtc(instant).getDayOfMonth());
		}

		@Override
		public String label(Instant instant) {
			return Month.tag(instant) + "-" + String.format("%02d", toUtc(instant).getDayOfMonth());
		}

		@Override
		public Instant plus(Instant instant) {
			return toUtc(instant).plusDays(1).toInstant(ZoneOffset.UTC);
		}

		@Override
		public Instant minus(Instant instant) {
			return toUtc(instant).minusDays(1).toInstant(ZoneOffset.UTC);
		}
	},
	Hour {
		@Override
		public String tag(Instant instant) {
			return Day.tag(instant) + "" + String.format("%02d", toUtc(instant).getHour());
		}

		@Override
		public String label(Instant instant) {
			return Day.tag(instant) + "-" + String.format("%02d", toUtc(instant).getHour());
		}

		@Override
		public Instant plus(Instant instant) {
			return toUtc(instant).plusHours(1).toInstant(ZoneOffset.UTC);
		}

		@Override
		public Instant minus(Instant instant) {
			return toUtc(instant).minusHours(1).toInstant(ZoneOffset.UTC);
		}
	};

	private static LocalDateTime toUtc(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
	}


	public abstract String tag(Instant instant);

	public abstract String label(Instant instant);

	public Instant plus(Instant instant) {
		return null;
	}

	public Instant minus(Instant instant) {
		return null;
	}
}
