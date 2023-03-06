package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.magritte.framework.Node;
import io.intino.ness.datahubterminalplugin.util.ErrorUtils;
import io.intino.plugin.PluginLauncher;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.customize;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;
import static java.io.File.separator;
import static java.util.stream.Collectors.toMap;

public class DatamartsRenderer {
	private static final String DOT = ".";
	private static final String JAVA = ".java";

	private final File srcFolder;
	private final NessGraph model;
	private final Configuration conf;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private String modelPackage;
	private final Templates templates;

	public DatamartsRenderer(File srcDir, NessGraph model, Configuration conf, PrintStream logger, PluginLauncher.Notifier notifier, String basePackage) {
		this.srcFolder = srcDir;
		this.model = model;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		srcFolder.mkdirs();
		this.modelPackage = basePackage;
		this.templates = new Templates();
	}

	public void render() {
		String basePackage = modelPackage + ".datamarts";
		for(Datamart datamart : model.datamartList()) {
			this.modelPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderDatamart(datamart);
			renderOntologyClassesOf(datamart);
		}
	}

	private void renderDatamart(Datamart datamart) {
		try {
			write(renderClassesOf(datamart));
		} catch (Throwable e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			throw new RuntimeException(e);
		}
	}

	private void renderOntologyClassesOf(Datamart datamart) {
		try {
			logger.println("Rendering entities and structs of " + datamart.name$() + "...");
			write(structClassesOf(datamart));
			write(entityClassesOf(datamart));
			write(entityMounterClassesOf(datamart));
		} catch (Exception e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> entityMounterClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(destination(baseEntityMounterName(datamart)), templates.entityMounter.render(entityMounterInterface(datamart)));
		datamart.entityList().forEach(e -> outputs.putAll(renderEntityMounter(e, datamart)));
		return outputs;
	}

	private String baseEntityMounterName(Datamart datamart) {
		return modelPackage + "." + javaValidName().format(firstUpperCase(datamart.name$()) + "Mounter").toString();
	}

	private FrameBuilder entityMounterInterface(Datamart datamart) {
		return new FrameBuilder("mounter", "interface")
				.add("package", modelPackage)
				.add("datamart", datamart.name$());
	}

	private Map<String, String> entityClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(entityDestination(entityBaseName(datamart)), templates.entityBase.render(entityBaseBuilder(datamart)));
		datamart.entityList().forEach(e -> outputs.putAll(renderEntity(e, datamart)));
		return outputs;
	}

	private String entityBaseName(Datamart datamart) {
		return modelPackage + "." + javaValidName().format(firstUpperCase(datamart.name$()) + "Entity").toString();
	}

	private FrameBuilder entityBaseBuilder(Datamart datamart) {
		return new FrameBuilder("entity", "base")
				.add("package", modelPackage)
				.add("datamart", datamart.name$());
	}

	private Map<String, String> structClassesOf(Datamart datamart) {
		return datamart.structList().stream()
				.map(this::renderStruct)
				.flatMap(e -> e.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<String, String> renderClassesOf(Datamart datamart) {
		String name = datamart.name$();
		String theInterface = modelPackage + DOT + firstUpperCase().format(javaValidName().format(name + "Datamart").toString());
		String theImplementation = modelPackage + DOT + firstUpperCase().format(javaValidName().format(name + "DatamartImpl").toString());
		return Map.of(
				destination(theInterface), templates.datamart.render(datamartInterfaceBuilder(datamart).toFrame()),
				destination(theImplementation), templates.datamart.render(datamartImplBuilder(datamart).toFrame())
		);
	}

	private FrameBuilder datamartInterfaceBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "interface").add("package", modelPackage);
		builder.add("name", datamart.name$());
		builder.add("entity", entitiesOf(datamart));
		return builder;
	}

	private FrameBuilder datamartImplBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "message", "impl").add("package", modelPackage);
		builder.add("name", datamart.name$()).add("scale", datamart.scale().name());
		builder.add("entity", entitiesOf(datamart));
		builder.add("struct", structsOf(datamart));
		builder.add("numEntities", datamart.entityList().size());
		builder.add("numStructs", datamart.structList().size());
		if(!datamart.entityList().isEmpty()) {
			builder.add("importEntities", "import " + modelPackage + ".entities.*;");
			builder.add("importMounters", "import " + modelPackage + ".mounters.*;");
		}
		if(!datamart.structList().isEmpty()) {
			builder.add("importStructs", "import " + modelPackage + ".structs.*;");
		}
		return builder;
	}

	private Frame[] entitiesOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage);
					b.add("name", c.name$()).add("fullName", fullNameOf(c));
					b.add("attribute", attributeFrames(c.attributeList().stream().map(Layer::core$), datamart));
					if(c.isExtensionOf()) b.add("parent", c.asExtensionOf().entity().name$());
					if (c.isAbstract()) {
						b.add("abstract");
						Frame[] subclasses = subclassesOf(c, datamart);
						if (subclasses.length > 0) {
							b.add("subclass", subclasses);
						}
					}
					b.add("isAbstract", c.isAbstract());
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Frame[] structsOf(Datamart datamart) {
		return datamart.structList().stream()
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("struct").add("package", modelPackage);
					b.add("name", c.name$()).add("fullName", fullNameOf(c));
					b.add("attribute", attributeFrames(c.attributeList().stream().map(Layer::core$), datamart));
					if(c.isExtensionOf()) b.add("parent", c.asExtensionOf().struct().name$());
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Frame[] attributeFrames(Stream<Node> attributes, Datamart datamart) {
		return attributes
				.map(a -> a.is(EntityData.class)
						? attributeFrameBuilder(a.as(EntityData.class), datamart)
						: attributeFrameBuilder(a.as(StructData.class), datamart))
				.map(FrameBuilder::toFrame)
				.toArray(Frame[]::new);
	}

	private FrameBuilder attributeFrameBuilder(EntityData attr, Datamart datamart) {

		FrameBuilder b = new FrameBuilder("attribute").add("name", attr.name$());

		if(attr.isList() || attr.isSet()) {
			b.add("type", attr.isList() ? "List" : "Set");
			b.add("collection");
			String parameterType = typeOf(attr);
			String parameterTypeName = parameterType;
			if(attr.isEntity()) {
				parameterType = modelPackage + ".entities." + attr.asEntity().entity().name$();
				parameterTypeName = attr.asEntity().entity().name$();
			}
			b.add("parameterType", parameterType);
			b.add("parameterTypeName", parameterTypeName);
			FrameBuilder param = new FrameBuilder("parameter").add("name", parameterTypeName);
			if(attr.isEntity()) param.add("entity");
			else if(attr.isStruct()) param.add("struct");
			b.add("parameter", param);
		} else if(attr.isEntity()) {
			b.add("type", modelPackage + ".entities." + attr.asEntity().entity().name$());
		} else if(attr.isMap()) {
			b.add("type", "Map").add("collection").add("parameterType", "java.lang.String");
		}

		return b;
	}

	private FrameBuilder attributeFrameBuilder(StructData attr, Datamart datamart) {
		return new FrameBuilder("attribute")
				.add("name", attr.name$())
				.add("type", typeOf(attr));
	}

	private String typeOf(EntityData node) {
		if(node.isDouble()) return "Double";
		if(node.isInteger()) return "Integer";
		if(node.isLong()) return "Long";
		if(node.isBoolean()) return "Boolean";
		if(node.isString()) return "String";
		if(node.isDate()) return "LocalDate";
		if(node.isDateTime()) return "LocalDateTime";
		if(node.isInstant()) return "Instant";
		if(node.isWord()) return node.asWord().name$();
		if(node.isStruct()) return node.asStruct().struct().name$();
		if(node.isEntity()) return node.asEntity().entity().name$();
		throw new RuntimeException("Unknown type of " + node.name$());
	}

	private String typeOfWithCollections(EntityData node) {
		if(node.isList()) return "List";
		if(node.isSet()) return "Set";
		if(node.isMap()) return "Map";
		return typeOf(node);
	}

	private String typeOf(StructData node) {
		if(node.isDouble()) return "Double";
		if(node.isInteger()) return "Integer";
		if(node.isLong()) return "Long";
		if(node.isBoolean()) return "Boolean";
		if(node.isString()) return "String";
		if(node.isDate()) return "LocalDate";
		if(node.isDateTime()) return "LocalDateTime";
		if(node.isInstant()) return "Instant";
		if(node.isWord()) return node.asWord().name$();
		if(node.isEntity()) return node.asEntity().entity().name$();
		throw new RuntimeException("Unknown type of " + node.name$());
	}

	private String fullNameOf(Entity e) {
		if(!e.isExtensionOf()) return e.name$();
		List<String> names = new ArrayList<>(4);
		Entity parent = e.asExtensionOf().entity();
		while(parent != null) {
			names.add(parent.name$());
			parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
		}
		Collections.reverse(names);
		names.add(e.name$());
		return String.join(".", names);
	}

	private String fullNameOf(Struct e) {
		if(!e.isExtensionOf()) return e.name$();

		List<String> names = new ArrayList<>(4);

		Struct parent = e.asExtensionOf().struct();
		while(parent != null) {
			names.add(parent.name$());
			parent = parent.isExtensionOf() ? parent.asExtensionOf().struct() : null;
		}
		names.add(e.name$());

		Collections.reverse(names);
		return String.join(".", names);
	}

	private Frame[] subclassesOf(Entity parent, Datamart datamart) {
		return datamart.entityList().stream()
				.filter(e -> isSubclassOf(e, parent))
				.map(c -> new FrameBuilder("subclass").add("package", modelPackage + ".master")
						.add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private static boolean isSubclassOf(Entity node, Entity expectedParent) {
		if(!node.isExtensionOf()) return false;
		Entity parent = node.asExtensionOf().entity();
		return parent.equals(expectedParent) || isSubclassOf(parent, expectedParent);
	}

	private Map<String, String> renderEntity(Entity entity, Datamart datamart) {
		return new EntityFrameFactory(modelPackage, datamart).create(entity).entrySet().stream()
				.collect(toMap(
						e -> entityDestination(e.getKey()),
						e -> templates.entity.render(e.getValue()))
				);
	}

	private Map<String, String> renderStruct(Struct struct) {
		return new StructFrameFactory(modelPackage).create(struct).entrySet().stream()
				.collect(toMap(
						e -> destination(e.getKey()),
						e -> templates.struct.render(e.getValue()))
				);
	}

	private Map<String, String> renderEntityMounter(Entity entity, Datamart datamart) {
		return new EntityMounterFrameFactory(modelPackage, datamart).create(entity).entrySet().stream()
				.collect(toMap(
						e -> entityDestination(e.getKey()),
						e -> templates.entityMounter.render(e.getValue()))
				);
	}

	private void write(Map<String, String> outputsMap) {
		outputsMap.forEach((key, value) -> {
			File file = new File(key);
			if (value.isEmpty() || isUnderSource(file) && file.exists()) return;
			file.getParentFile().mkdirs();
			write(file, value);
		});
	}

	private boolean isUnderSource(File file) {
		return file.getAbsolutePath().startsWith(srcFolder.getAbsolutePath());
	}

	private void write(File file, String text) {
		try {
			file.getParentFile().mkdirs();
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
			fileWriter.write(text);
			fileWriter.close();
		} catch (IOException e) {
			notifier.notifyError(e.getMessage());
		}
	}

	private String destination(String path) {
		return new File(srcFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private String entityDestination(String path) {
		return new File(srcFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private static class Templates {
		final Template datamart = customize(new DatamartTemplate());
		final Template entityBase = customize(new EntityBaseTemplate());
		final Template entity = customize(new EntityTemplate());
		final Template entityMounter = customize(new EntityMounterTemplate());
		final Template struct = customize(new StructTemplate());
	}
}
