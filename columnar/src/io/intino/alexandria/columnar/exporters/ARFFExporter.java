package io.intino.alexandria.columnar.exporters;

import io.intino.alexandria.columnar.Column;
import io.intino.alexandria.columnar.Column.ColumnType;
import io.intino.alexandria.columnar.Columnar;
import io.intino.alexandria.columnar.Row;
import org.siani.itrules.model.Frame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ARFFExporter {
	private static final String NULL_VALUE = "?";
	private final Iterator<Row> iterator;
	private final List<Column> columns;
	private final Columnar.Select.RowFilter filter;

	public ARFFExporter(Iterator<Row> iterator, List<Column> columns, Columnar.Select.RowFilter filter) {
		this.iterator = iterator;
		this.columns = columns;
		this.filter = filter;
	}

	public void export(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(ArffTemplate.create().format(new Frame("arff").addSlot("attribute", attributes())));
		while (iterator.hasNext()) {
			Row row = iterator.next();
			if (!filter.test(row)) continue;
			writer.write(String.join(",", format(row)) + "\n");
		}
		writer.close();
	}

	private List<String> format(Row row) {
		List<String> values = new ArrayList<>();
		values.add(row.id() + "");
		values.add(row.timetag().value());
		values.addAll(columns.stream().map(column -> format(column, row.get(column.name()))).collect(Collectors.toList()));
		return values;
	}

	private String format(Column column, String value) {
		if (value == null) return NULL_VALUE;
		if (column.type() instanceof ColumnType.String || column.type() instanceof ColumnType.Nominal) return "'" + value + "'";
		else if (column.type() instanceof ColumnType.Date) return "\"" + value + "\"";
		return value;
	}

	private Frame[] attributes() {
		List<Frame> headers = new ArrayList<>();
		headers.add(new Frame("attribute").addSlot("name", "id").addSlot("type", new Frame("Numeric")));
		headers.add(new Frame("attribute").addSlot("name", "timetag").addSlot("type", new Frame("Date").addSlot("format", "yyyyMM")));
		for (Column column : columns)
			headers.add(new Frame("attribute").addSlot("name", column).addSlot("type", columnType(column.type())));
		return headers.toArray(new Frame[0]);
	}

	private Frame columnType(ColumnType type) {
		if (type instanceof ColumnType.Numeric) return new Frame("Numeric");
		if (type instanceof ColumnType.Date) return new Frame("Date").addSlot("format", ((ColumnType.Date) type).format());
		if (type instanceof ColumnType.Nominal) return new Frame("Nominal").addSlot("value", ((ColumnType.Nominal) type).values());
		return new Frame("String");
	}
}
