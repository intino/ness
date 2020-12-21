package io.intino.ness.datahubterminalplugin.renders.lookups;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Data;
import io.intino.datahub.graph.Lookup;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.renders.Formatters;
import io.intino.ness.datahubterminalplugin.renders.terminals.LookupsTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.intino.ness.datahubterminalplugin.Commons.writeFrame;
import static io.intino.ness.datahubterminalplugin.renders.Formatters.firstUpperCase;

public class LookupRenderer {
	private final File destination;
	private final List<File> resDirectories;
	private final String rootPackage;

	public LookupRenderer(File destination, List<File> resDirectories, String rootPackage) {
		this.destination = destination;
		this.resDirectories = resDirectories;
		this.rootPackage = rootPackage;
	}

	public void render(Lookup lookup) {
		String rootPackage = lookupsPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = buildFrame(lookup);
		Commons.writeFrame(packageFolder, lookup.name$(), template(lookup).render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("lookup", frame)));
	}

	public void renderLookupsClass(List<Lookup> lookups) {
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		if (!lookups.isEmpty())
			writeFrame(packageFolder, "Lookups", lookupsClassTemplate().render(renderLookups(lookups.stream().filter(Lookup::isDynamic).map(Lookup::asDynamic))));
	}

	private Frame renderLookups(Stream<Lookup.Dynamic> lookups) {
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

	private void renderLookup(String lookupsPackage, FrameBuilder fb, Lookup.Dynamic l) {
		fb.add("lookup", new FrameBuilder("lookup").
				add("qn", lookupsPackage + "." + firstUpperCase(l.name$())).
				add("namespace", l.namespace()).
				add("name", l.name$()));
	}

	private Frame buildFrame(Lookup lookup) {
		FrameBuilder builder = new FrameBuilder("lookup").
				add("name", lookup.name$()).
				add("type", lookup.isResource() ? String.class.getSimpleName() : lookup.name$());
		builder.add("rootPackage", this.rootPackage);
		if (lookup.isResource()) asResource(lookup, builder);
		else if (lookup.isEnumerate()) asEnumerate(lookup, builder);
		else if (lookup.isDynamic()) asDynamic(lookup, builder);
		return builder.toFrame();
	}

	private void asResource(Lookup lookup, FrameBuilder builder) {
		builder.add("resource").add("resource", resourceFile(lookup));
		List<Lookup.Resource.Column> columnList = lookup.asResource().columnList();
		addResourceColumns(builder, columnList);
	}

	private void asEnumerate(Lookup lookup, FrameBuilder builder) {
		builder.add("enumerate").add("entry", entries(lookup));
	}

	private void asDynamic(Lookup lookup, FrameBuilder builder) {
		builder.add("dynamic");
		addDynamicColumns(builder, lookup.asDynamic().columnList());
	}

	private void addResourceColumns(FrameBuilder builder, List<Lookup.Resource.Column> columnList) {
		for (int i = 0; i < columnList.size(); i++) {
			boolean primitive = isPrimitive(columnList.get(i).asType());
			FrameBuilder b = new FrameBuilder("column").
					add("name", columnList.get(i).name$()).
					add("index", i).
					add("type", primitive ? columnList.get(i).asType().primitive() : columnList.get(i).asType().type());
			columnList.get(i).core$().conceptList().stream().filter(Concept::isAspect).map(Predicate::name).forEach(b::add);
			if (primitive) b.add("primitive");
			builder.add("column", b.toFrame());
		}
	}

	private void addDynamicColumns(FrameBuilder builder, List<Lookup.Dynamic.Column> columnList) {
		Lookup.Dynamic.Column idColumn = columnList.stream().filter(Data::isId).findFirst().orElse(null);
		if (idColumn == null) return;
		boolean idPrimitive = isPrimitive(idColumn.asType());
		for (int i = 0; i < columnList.size(); i++) {
			Lookup.Dynamic.Column column = columnList.get(i);
			FrameBuilder b = new FrameBuilder("column").add(isPrimitive(column.asType()) ? "primitive" : "complex").add(column.isId() ? "id" : "regular").
					add("name", column.name$()).
					add("index", i).
					add("type", type(column)).
					add("typePrimitive", typePrimitive(column)).
					add("defaultValue", column.asType().defaultValueString()).
					add("idColumnName", idColumn.name$()).
					add("idColumnType", idPrimitive ? idColumn.asType().primitive() : idColumn.asType().type());
			if (column.isCategory()) b.add("category").add("lookup", column.asCategory().lookup().name$());
			column.core$().conceptList().stream().filter(Concept::isAspect).map(Predicate::name).forEach(b::add);
			builder.add("column", b.toFrame());
		}
	}

	private String typePrimitive(Lookup.Dynamic.Column column) {
		if (isPrimitive(column.asType())) return column.asType().primitive();
		if (column.isCategory()) return "int";
		return column.asType().type();
	}

	private String type(Lookup.Dynamic.Column column) {
		if (isPrimitive(column.asType())) return column.asType().primitive();
		if (column.isCategory()) return firstUpperCase(column.asCategory().lookup().name$()) + ".Entry";
		return column.asType().type();
	}

	private boolean isPrimitive(Data.Type attribute) {
		Data data = attribute.asData();
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isReal();
	}

	private String resourceFile(Lookup lookup) {
		try {
			Path source = new File(lookup.asResource().tsv().getPath()).getCanonicalFile().toPath();
			for (File resDirectory : resDirectories) {
				try {
					return resDirectory.toPath().relativize(source).toFile().getPath().replace("\\", "/");
				} catch (IllegalArgumentException ex) {
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	private Frame[] entries(Lookup lookup) {
		return lookup.asEnumerate().itemList().stream().
				map(e -> new FrameBuilder("entry").add("name", e.name$()).add("index", e.index()).add("label", e.label()).toFrame()).toArray(Frame[]::new);
	}

	private Template template(Lookup lookup) {
		return Formatters.customize(lookup.isDynamic() ? new DynamicLookupTemplate() : new LookupTemplate());
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
