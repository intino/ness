package io.intino.alexandria.columnar;

import java.util.List;

public class CompositeColumn extends Column {

	private final List<Column> columns;

	public CompositeColumn(String name, List<Column> columns) {
		super(name, columns.get(0).type());
		this.columns = columns;
	}

	public CompositeColumn(String name, List<Column> columns, Mapper mapper) {
		super(name, columns.get(0).type(), mapper);
		this.columns = columns;
	}

	public List<Column> columns() {
		return columns;
	}
}
