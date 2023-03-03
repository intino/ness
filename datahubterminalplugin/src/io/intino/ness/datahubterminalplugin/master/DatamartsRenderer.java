package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.util.ErrorUtils;
import io.intino.plugin.PluginLauncher;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
	private String nodeImplName = "memorymap";

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
			this.modelPackage = basePackage + datamart.name$().toLowerCase();
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
//			write(entityMounterClassesOf(datamart));
		} catch (Exception e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> entityMounterClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		datamart.entityList().forEach(e -> outputs.putAll(renderEntityMounter(e, datamart)));
		return outputs;
	}

	private Map<String, String> entityClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(entityDestination(entityBaseName(datamart)), templates.entityBase.render(entityBaseBuilder(datamart)));
		datamart.entityList().forEach(e -> outputs.putAll(renderEntity(e, datamart)));
		return outputs;
	}

	private String entityBaseName(Datamart datamart) {
		return javaValidName().format(firstUpperCase(datamart.name$()) + "Entity").toString();
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
		String theNodeImplementation = modelPackage + DOT + firstUpperCase().format(javaValidName().format(name + "DatamartImpl").toString());
		return Map.of(
				destination(theNodeImplementation), templates.nodeImpl.render(nodeImplBuilder(datamart).toFrame()),
				destination(theInterface), templates.datamart.render(datamartInterfaceBuilder(datamart).toFrame()),
				destination(theImplementation), templates.datamart.render(datamartImplBuilder(datamart).toFrame())
		);
	}

	private FrameBuilder nodeImplBuilder(Datamart datamart) {
		return new FrameBuilder("node", nodeImplName);
	}

	private FrameBuilder datamartInterfaceBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "interface").add("package", modelPackage);
		builder.add("entity", entitiesOf(datamart));
		return builder;
	}

	private FrameBuilder datamartImplBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "impl").add("package", modelPackage);
		builder.add("entity", entitiesOf(datamart));
		builder.add("nodeImpl", getNodeImplClass());
		return builder;
	}

	private String getNodeImplClass() {
		if(nodeImplName.equals("memorymap")) return "MemoryMapNode";
		throw new RuntimeException("Unsupported NodeImplName: " + nodeImplName);
	}

	private Frame[] entitiesOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage).add("name", c.name$());
					if (c.isAbstract()) {
						b.add("abstract");
						Frame[] subclasses = subclassesOf(c, datamart);
						if (subclasses.length > 0) b.add("subclass", subclasses);
					}
					return b.toFrame();
				}).toArray(Frame[]::new);
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
						e -> templates.entity.render(e.getValue()))
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
		final Template nodeImpl = customize(new DatamartNodeImplTemplate());
		final Template entityBase = customize(new EntityBaseTemplate());
		final Template entity = customize(new EntityTemplate());
		final Template entityMounter = customize(new EntityMounterTemplate());
		final Template struct = customize(new StructTemplate());
	}
}
