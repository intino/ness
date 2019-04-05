package io.intino.alexandria.columnar.exporters;

import com.opencsv.CSVWriter;
import io.intino.alexandria.columnar.Column;
import io.intino.alexandria.columnar.Columnar;
import io.intino.alexandria.columnar.Exporter;
import io.intino.alexandria.columnar.Row;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.opencsv.CSVWriter.NO_QUOTE_CHARACTER;

public class CSVExporter implements Exporter {
	private static final String NULL_VALUE = "";
	private final Iterator<Row> iterator;
	private final List<Column> columns;
	private final Columnar.Select.RowFilter filter;

	public CSVExporter(Iterator<Row> iterator, List<Column> columns, Columnar.Select.RowFilter filter) {
		this.iterator = iterator;
		this.columns = columns;
		this.filter = filter;
	}

	public void export(File file) throws IOException {
		CSVWriter csvWriter = new CSVWriter(new FileWriter(file), ';', NO_QUOTE_CHARACTER);
		csvWriter.writeNext(headers());
		while (iterator.hasNext()) {
			Row row = iterator.next();
			if (!filter.test(row)) continue;
			csvWriter.writeNext(format(row));
		}
		csvWriter.close();
	}

	private String[] format(Row row) {
		List<String> values = new ArrayList<>();
		values.add(row.id() + "");
		values.add(row.timetag().value());
		values.addAll(columns.stream().map(column -> format(row.get(column.name()))).collect(Collectors.toList()));
		return values.toArray(new String[0]);
	}

	private String format(String value) {
		return value == null ? NULL_VALUE : value;
	}

	private String[] headers() {
		List<String> header = new ArrayList<>();
		header.add("id");
		header.add("timetag");
		header.addAll(columns.stream().map(Column::name).collect(Collectors.toList()));
		return header.toArray(new String[0]);
	}
}
