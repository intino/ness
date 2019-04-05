package io.intino.alexandria.columnar;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.assa.AssaStream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

class RowIterator implements Iterator<Row> {
	private final Timetag timetag;
	private final Map<Column, TemporalReader> columns;

	@SuppressWarnings("ConstantConditions")
	RowIterator(Timetag timetag, List<AssaStream> readers, List<Column> columns) {
		this.timetag = timetag;
		this.columns = columns.stream().collect(Collectors.toMap(c -> c, c -> new TemporalReader(readers.get(columns.indexOf(c)))));
	}

	public Row next() {
		long lowestKey = lowestKey();
		Row row = new Row(lowestKey, timetag);
		for (Map.Entry<Column, TemporalReader> entry : columns.entrySet()) {
			if (entry.getValue().current == null || entry.getValue().current.key() != lowestKey)
				row.put(entry.getKey().name(), entry.getKey().map(emptyList()));
			else {
				row.put(entry.getKey().name(), entry.getKey().map(entry.getValue().current.value()));
				entry.getValue().next();
			}
		}
		advanceReadersWith(lowestKey);
		return row;
	}

	private void advanceReadersWith(long lowestKey) {
		columns.values().forEach(t -> {
			while (t.current.key() == lowestKey) t.next();
		});
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private long lowestKey() {
		return columns.values().stream().mapToLong(t -> t.current.key()).min().getAsLong();
	}

	public boolean hasNext() {
		return columns.values().stream().anyMatch(t -> t.current.key() != Long.MAX_VALUE);
	}

	private class TemporalReader {

		private final AssaStream reader;
		private AssaStream.Item current;

		TemporalReader(AssaStream reader) {
			this.reader = reader;
			next();
		}

		void next() {
			this.current = reader.hasNext() ? reader.next() : new AssaStream.Item() {
				@Override
				public long key() {
					return Long.MAX_VALUE;
				}

				@Override
				public List<String> value() {
					return emptyList();
				}
			};
		}
	}
}
