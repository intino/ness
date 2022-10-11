package io.intino.ness.datahubterminalplugin.lookups;

import io.intino.datahub.model.Data;
import io.intino.datahub.model.Lookup;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.intino.ness.datahubterminalplugin.Commons.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Commons.writeFrame;

public class LookupRenderer {
	private final File destination;
	private final String rootPackage;

	public LookupRenderer(File destination, String rootPackage) {
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render(Lookup lookup) {
		String rootPackage = lookupsPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = buildFrame(lookup);
		Commons.writeFrame(packageFolder, lookup.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("lookup", frame)));
	}

	public void renderLookupsClass(List<Lookup> lookups) {
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		if (!lookups.isEmpty())
			writeFrame(packageFolder, "Lookups", lookupsClassTemplate().render(renderLookups(lookups.stream())));
	}

	private Frame renderLookups(Stream<Lookup> lookups) {
		Set<String> namespaces = new LinkedHashSet<>();
		FrameBuilder fb = new FrameBuilder("lookups");
		fb.add("package", rootPackage);
		lookups.forEach(l -> {
			renderLookup(rootPackage + ".lookups", fb, l);
			namespaces.add(l.namespace());
		});
		fb.add("namespace", namespaces.toArray(new String[0]));
		return fb.toFrame();
	}

	private void renderLookup(String lookupsPackage, FrameBuilder fb, Lookup l) {
		fb.add("lookup", new FrameBuilder("lookup").
				add("qn", lookupsPackage + "." + firstUpperCase(l.name$())).
				add("namespace", l.namespace()).
				add("name", l.name$()));
	}

	private Frame buildFrame(Lookup lookup) {
		FrameBuilder builder = new FrameBuilder("lookup").
				add("name", lookup.name$()).
				add("type", lookup.name$());
		builder.add("rootPackage", this.rootPackage);
		buildFrame(lookup, builder);
		return builder.toFrame();
	}

	private void buildFrame(Lookup lookup, FrameBuilder builder) {
		builder.add("dynamic");
		Lookup.Column idColumn = lookup.columnList().stream().filter(Lookup.Column::isId).findFirst().orElse(null);
		addIndexes(builder, lookup.indexList(), idColumn);
		addDynamicColumns(builder, lookup.columnList());
		if (idColumn != null) {
			boolean idPrimitive = isPrimitive(idColumn.asType());
			builder.add("idColumnName", idColumn.name$()).add("idColumnType", idPrimitive ? idColumn.asType().primitive() : idColumn.asType().type());
		}
	}

	private void addIndexes(FrameBuilder builder, List<Lookup.Index> indices, Lookup.Column idColumn) {
		for (Lookup.Index index : indices) {
			Lookup lookup = index.core$().ownerAs(Lookup.class);
			List<Lookup.Column> columns = index.columns();
			builder.add("index", new FrameBuilder("index", index.createOnClose() ? "onClose" : "onOpen")
					.add("name", index.name$())
					.add("table", index.core$().ownerAs(Lookup.class).name$())
					.add("idxColumn", columns.stream().map(c -> frameOf(c, indexOf(c))).toArray(FrameBuilder[]::new))
					.add("column", lookup.columnList().stream().map(c -> frameOf(c, indexOf(c)).add(columns.contains(c) ? "idx" : "regular")).toArray(FrameBuilder[]::new)));
		}
		if (idColumn != null) builder.add("index", new FrameBuilder("index", "id", "onOpen")
				.add("name", idColumn.name$())
				.add("table", idColumn.core$().ownerAs(Lookup.class).name$())
				.add("idxColumn", frameOf(idColumn, indexOf(idColumn)))
				.add("column", idColumn.core$().ownerAs(Lookup.class).columnList().stream().map(c -> frameOf(c, indexOf(c)).add(c.equals(idColumn) ? "idx" : "regular")).toArray(FrameBuilder[]::new)));
	}

	private int indexOf(Lookup.Column c) {
		return c.core$().ownerAs(Lookup.class).columnList().indexOf(c);
	}

	private void addDynamicColumns(FrameBuilder builder, List<Lookup.Column> columnList) {
		Lookup.Column idColumn = columnList.stream().filter(Lookup.Column::isId).findFirst().orElse(null);
		for (int i = 0; i < columnList.size(); i++) {
			Lookup.Column column = columnList.get(i);
			FrameBuilder b = frameOf(column, i);
			if (idColumn != null)
				b.add("hasId").add("idColumnName", idColumn.name$()).add("idColumnType", isPrimitive(idColumn.asType()) ? idColumn.asType().primitive() : idColumn.asType().type());
			builder.add("column", b.toFrame());
		}
	}

	private FrameBuilder frameOf(Lookup.Column column, int index) {
		FrameBuilder builder = new FrameBuilder("column").
				add(column.isId() ? "id" : "regular").
				add(isPrimitive(column.asType()) ? "primitive" : "complex").
				add("table", column.core$().ownerAs(Lookup.class).name$()).
				add("name", column.name$()).
				add("index", index).
				add("type", type(column)).
				add("typePrimitive", typePrimitive(column));
		column.core$().conceptList().stream().filter(Concept::isAspect).map(Predicate::name).forEach(builder::add);
		if (column.isWordFromBag()) builder.add("category").add("lookup", column.asWordFromBag().wordbag().name$());
		return builder;
	}

	private String typePrimitive(Lookup.Column column) {
		if (isPrimitive(column.asType())) return column.asType().primitive();
		if (column.isWordFromBag()) return "int";
		return column.asType().type();
	}

	private String type(Lookup.Column column) {
		if (isPrimitive(column.asType())) return column.asType().primitive();
		if (column.isWordFromBag()) return firstUpperCase(column.asWordFromBag().wordbag().name$()) + ".Entry";
		return column.asType().type();
	}

	private boolean isPrimitive(Data.Type attribute) {
		Data data = attribute.asData();
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isReal();
	}

	private Template template() {
		return Formatters.customize(new LookupTemplate());
	}

	private String lookupsPackage() {
		return rootPackage + ".lookups";
	}

	private Template lookupsClassTemplate() {
		return Formatters.customize(new LookupsTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
