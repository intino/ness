package io.intino.ness.datahubterminalplugin.event;

import io.intino.datahub.graph.Table;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.List;

public class TableRenderer {
	private final Table table;
	private final File destination;
	private final String rootPackage;

	public TableRenderer(Table table, File destination, String rootPackage) {
		this.table = table;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = eventsPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createTableFrame(table);
		Commons.writeFrame(packageFolder, table.name$(), template().render(frame));
	}

	private Frame createTableFrame(Table table) {
		return new FrameBuilder("table")
				.add("package", eventsPackage())
				.add("name", table.name$())
				.add("column", columns(table.columnList()))
				.add("type", "table").toFrame();
	}

	private Frame[] columns(List<Table.Column> columns) {
		return columns.stream().map(c -> {
			FrameBuilder builder = new FrameBuilder("column", c.asType().getClass().getSimpleName()).
					add("name", c.name$()).
					add("simpleType", simpleType(c)).
					add("type", c.asType().type());
			if (c.isWord()) builder.add("word", c.asWord().values().toArray(String[]::new));
			return builder.toFrame();
		}).toArray(Frame[]::new);
	}

	private String simpleType(Table.Column c) {
		if (c.isWord()) return c.name$();
		String type = c.asType().type();
		return type.contains(".") ? type.substring(type.lastIndexOf(".") + 1) : type;
	}

	private String eventsPackage() {
		return rootPackage + ".events";
	}

	private Template template() {
		return Formatters.customize(new TableTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
