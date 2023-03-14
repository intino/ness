package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.util.ErrorUtils;
import io.intino.plugin.PluginLauncher;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.customize;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;
import static java.io.File.separator;
import static java.util.stream.Collectors.toMap;

public class DatamartsRenderer implements ConceptRenderer {
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

	public void render(Terminal terminal, String terminalPackage) {
		TerminalInfo terminalInfo = new TerminalInfo(terminal, terminalPackage);
		String basePackage = modelPackage + ".datamarts";
		for(Datamart datamart : terminal.datamarts().list()) {
			this.modelPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderDatamart(datamart, terminalInfo);
		}
	}

	private void renderDatamart(Datamart datamart, TerminalInfo terminalInfo) {
		try {
			write(renderImplementationOf(datamart, terminalInfo));
			write(entityMounterClassesOf(datamart, terminalInfo));
		} catch (Throwable e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			throw new RuntimeException(e);
		}
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
			write(renderInterfaceOf(datamart));
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
		} catch (Exception e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> entityMounterClassesOf(Datamart datamart, TerminalInfo terminalInfo) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(destination(baseEntityMounterName(datamart, terminalInfo)), templates.entityMounter.render(entityMounterInterface(datamart, terminalInfo)));
		datamart.entityList().forEach(e -> outputs.putAll(renderEntityMounter(e, datamart, terminalInfo)));
		return outputs;
	}

	private String baseEntityMounterName(Datamart datamart, TerminalInfo terminalInfo) {
		return terminalInfo.terminalPackage + "." + subPackageOf(datamart) + "." + firstUpperCase(datamart.name$()) + "Mounter";
	}

	private FrameBuilder entityMounterInterface(Datamart datamart, TerminalInfo terminalInfo) {
		return new FrameBuilder("mounter", "interface")
				.add("package", terminalInfo.terminalPackage + subPackageOf(datamart))
				.add("ontologypackage", modelPackage)
				.add("datamart", datamart.name$());
	}

	private String subPackageOf(Datamart datamart) {
		return ".datamarts." + javaValidName().format(datamart.name$().toLowerCase());
	}

	private Map<String, String> entityClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		renderEntityBase(datamart, outputs);
		datamart.entityList().forEach(e -> outputs.putAll(renderEntity(e, datamart)));
		return outputs;
	}

	private void renderEntityBase(Datamart datamart, Map<String, String> outputs) {
		outputs.put(entityDestination(entityBaseName(datamart)), templates.entityBase.render(entityBaseBuilder(datamart)));
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
		Map<String, String> output = new HashMap<>();
		renderStructBase(datamart, output);
		datamart.structList().stream()
				.map(struct -> renderStruct(datamart, struct))
				.forEach(output::putAll);
		return output;
	}

	private void renderStructBase(Datamart datamart, Map<String, String> output) {
		output.put(destination(structBaseName(datamart)), templates.structBase.render(structBaseBuilder(datamart)));
	}

	private String structBaseName(Datamart datamart) {
		return modelPackage + "." + javaValidName().format(firstUpperCase(datamart.name$()) + "Struct").toString();
	}

	private FrameBuilder structBaseBuilder(Datamart datamart) {
		return new FrameBuilder("struct", "base")
				.add("package", modelPackage)
				.add("datamart", datamart.name$());
	}

	private Map<String, String> renderInterfaceOf(Datamart datamart) {
		String theInterface = modelPackage + DOT + firstUpperCase().format(javaValidName().format(datamart.name$() + "Datamart").toString());
		return Map.of(destination(theInterface), templates.datamart.render(datamartInterfaceBuilder(datamart).toFrame()));
	}

	private Map<String, String> renderImplementationOf(Datamart datamart, TerminalInfo terminalInfo) {
		String theImplementation = modelPackage + DOT + firstUpperCase().format(javaValidName().format(datamart.name$() + "DatamartImpl").toString());
		return Map.of(destination(theImplementation), templates.datamart.render(datamartImplBuilder(datamart, terminalInfo).toFrame()));
	}

	private FrameBuilder datamartInterfaceBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "interface");
		builder.add("package", modelPackage);
		builder.add("name", datamart.name$());
		builder.add("entity", entitiesOf(datamart));
		return builder;
	}

	private FrameBuilder datamartImplBuilder(Datamart datamart, TerminalInfo terminalInfo) {
		FrameBuilder builder = new FrameBuilder("datamart", "message", "impl");
		builder.add("package", terminalInfo.terminalPackage + subPackageOf(datamart));
		builder.add("name", datamart.name$()).add("scale", datamart.scale().name());
		builder.add("entity", entitiesOf(datamart));
		builder.add("struct", structsOf(datamart));
		builder.add("numEntities", datamart.entityList().size());
		builder.add("numStructs", datamart.structList().size());
		builder.add("ontologypackage", modelPackage);
		return builder;
	}

	private Frame[] entitiesOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(entity -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage);
					b.add("name", entity.name$()).add("fullName", fullNameOf(entity));
					b.add("attribute", attributeFrames(attributesOf(entity)));
					if(entity.from() != null) b.add("event", entity.from().message().name$());
					if(entity.isExtensionOf()) b.add("parent", entity.asExtensionOf().entity().name$());
					if (entity.isAbstract()) {
						b.add("abstract");
						Frame[] subclasses = subclassesOf(entity, datamart);
						if (subclasses.length > 0) {
							b.add("subclass", subclasses);
						}
					}
					b.add("isAbstract", entity.isAbstract());
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Frame[] structsOf(Datamart datamart) {
		return datamart.structList().stream()
				.map(struct -> {
					final FrameBuilder b = new FrameBuilder("struct").add("package", modelPackage);
					b.add("name", struct.name$()).add("fullName", fullNameOf(struct));
					b.add("attribute", attributeFrames(attributesOf(struct)));
					if(struct.isExtensionOf()) b.add("parent", struct.asExtensionOf().struct().name$());
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Frame[] attributeFrames(List<ConceptAttribute> attributes) {
		return attributes.stream()
				.map(this::attributeFrameBuilder)
				.map(FrameBuilder::toFrame)
				.toArray(Frame[]::new);
	}

	private FrameBuilder attributeFrameBuilder(ConceptAttribute attr) {
		FrameBuilder b = new FrameBuilder("attribute").add("name", attr.name$());

		if(attr.isList() || attr.isSet()) {
			setAttribCollectionInfo(attr, b);
		} else if(attr.isMap()) {
			b.add("type", "java.util.Map").add("collection").add("parameterTypeName", "java.lang.String").add("parameterType", "java.lang.String");
			b.add("parameter", new FrameBuilder("parameter"));
		} else if(attr.isEntity()) {
			b.add("type", modelPackage + ".entities." + attr.asEntity().entity().name$());
		}  else if(attr.isStruct()) {
			b.add("type", modelPackage + ".structs." + attr.asStruct().struct().name$());
		} else if(attr.isWord()) {
			b.add("type", modelPackage + ".entities." + attr.owner().name() + "." + attr.type());
		} else {
			b.add("type", attr.type());
		}

		return b;
	}

	private void setAttribCollectionInfo(ConceptAttribute attr, FrameBuilder b) {
		b.add("type", attr.isList() ? "java.util.List" : "java.util.Set");
		b.add("collection");
		String parameterType = attr.type();
		String parameterTypeName = parameterType;
		if(attr.isEntity()) {
			parameterType = modelPackage + ".entities." + attr.asEntity().entity().name$();
			parameterTypeName = attr.asEntity().entity().name$();
		} else if(attr.isStruct()) {
			parameterType = modelPackage + ".structs." + attr.asStruct().struct().name$();
			parameterTypeName = attr.asStruct().struct().name$();
		} else if(attr.isWord()) {
			parameterType = modelPackage + ".entities." + attr.owner().name() + "." + parameterType;
		}
		b.add("parameterType", parameterType);
		b.add("parameterTypeName", parameterTypeName);
		FrameBuilder param = new FrameBuilder("parameter").add("name", parameterTypeName);
		if(attr.isEntity()) param.add("entity");
		else if(attr.isStruct()) param.add("struct");
		else if(attr.isWord()) param.add("word");
		b.add("parameter", param);
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

	private Map<String, String> renderStruct(Datamart datamart, Struct struct) {
		return new StructFrameFactory(datamart, modelPackage).create(struct).entrySet().stream()
				.collect(toMap(
						e -> destination(e.getKey()),
						e -> templates.struct.render(e.getValue()))
				);
	}

	private Map<String, String> renderEntityMounter(Entity entity, Datamart datamart, TerminalInfo terminalInfo) {
		return new EntityMounterFrameFactory(terminalInfo.terminalPackage + subPackageOf(datamart), modelPackage, datamart)
				.create(entity).entrySet().stream()
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

	public record TerminalInfo(Terminal terminal, String terminalPackage) { }

	private static class Templates {
		final Template datamart = customize(new DatamartTemplate());
		final Template entityBase = customize(new EntityBaseTemplate());
		final Template entity = customize(new EntityTemplate());
		final Template entityMounter = customize(new EntityMounterTemplate());
		final Template struct = customize(new StructTemplate());
		final Template structBase = customize(new StructBaseTemplate());
	}
}
