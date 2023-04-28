package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.util.ErrorUtils;
import io.intino.plugin.PluginLauncher;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.customize;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;
import static io.intino.ness.datahubterminalplugin.master.StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP;
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
		builder.add("name", datamart.name$()).add("scale", datamart.snapshots().scale().name());
		builder.add("entity", entitiesOf(datamart));
		builder.add("struct", structsOf(datamart));
		builder.add("numEntities", datamart.entityList().size());
		builder.add("numStructs", datamart.structList().size());
		builder.add("ontologypackage", modelPackage);
		builder.add("terminal", String.format(terminalInfo.terminalPackage + "." + firstUpperCase(javaValidName().format(terminalInfo.terminal.name$()).toString())));
		return builder;
	}

	private Frame[] structsOf(Datamart datamart) {
		List<Frame> structFrames = datamart.structList().stream()
				.flatMap(struct -> framesOf(datamart, struct, modelPackage + ".structs", null))
				.collect(Collectors.toList());

		for(Entity entity : datamart.entityList()) {
			entity.structList().stream()
					.flatMap(struct -> framesOf(datamart, struct, modelPackage + ".entities." + entity.name$(), entity.name$()))
					.forEach(structFrames::add);
		}

		return structFrames.toArray(Frame[]::new);
	}

	private Stream<Frame> framesOf(Datamart datamart, Struct struct, String thePackage, String owner) {
		String fullname = owner == null ? fullNameOf(struct) : owner + STRUCT_INTERNAL_CLASS_SEP + fullNameOf(struct);
		FrameBuilder b = new FrameBuilder("struct");
		b.add("package", thePackage);
		b.add("name", struct.name$());
		b.add("fullName", fullname);
		b.add("attribute", attributeFrames(attributesOf(struct, (a, o) -> new ConceptAttribute(a, o) {
			@Override
			public String ownerFullName() {
				return fullname.replace(STRUCT_INTERNAL_CLASS_SEP, ".");
			}
		})));
//		if(struct.isExtensionOf()) {
//			b.add("parent", struct.asExtensionOf().struct().name$());
//			b.add("ancestor", ancestorsOf(struct));
//		}
//		setDescendantsInfo(datamart, struct, b);

		List<Frame> frames = new ArrayList<>(1);
		frames.add(b.toFrame());

		for(Struct s : struct.structList()) framesOf(datamart, s, thePackage + "." + struct.name$(), fullname).forEach(frames::add);

		return frames.stream();
	}

	private Frame[] entitiesOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(entity -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage);
					b.add("name", entity.name$()).add("fullName", fullNameOf(entity));
					b.add("attribute", attributeFrames(attributesOf(entity)));
					if(entity.from() != null) b.add("event", entity.from().message().name$());
					if(entity.isExtensionOf()) {
						b.add("parent", entity.asExtensionOf().entity().name$());
						b.add("ancestor", ancestorsOf(entity));
					} else {
						b.add("hasNoParents", "true");
					}
					if (entity.isAbstract()) b.add("abstract");
					b.add("isAbstract", entity.isAbstract());
					setDescendantsInfo(datamart, entity, b);
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	@Override
	public List<ConceptAttribute> attributesOf(Entity entity) {
		List<ConceptAttribute> attributes = ConceptRenderer.super.attributesOf(entity);
		entity.structList().stream().map(struct -> attrOf(entity.core$(), struct)).forEach(attributes::add);
		return attributes;
	}

	@Override
	public List<ConceptAttribute> attributesOf(Struct struct) {
		List<ConceptAttribute> attributes = ConceptRenderer.super.attributesOf(struct);
		struct.structList().stream().map(s -> attrOf(struct.core$(), s)).forEach(attributes::add);
		return attributes;
	}

	private void setDescendantsInfo(Datamart datamart, Entity entity, FrameBuilder b) {
		Entity[] descendants = descendantsOf(entity, datamart);
		if(descendants.length == 0) return;
		b.add("superclass");
		b.add("descendant", Arrays.stream(descendants).map(e -> new FrameBuilder("descendant").add("entity").add("name", e.name$()).toFrame()).toArray(Frame[]::new));
		b.add("subclass", framesOfUpperLevelDescendants(entity, datamart));
	}

	private void setDescendantsInfo(Datamart datamart, Struct struct, FrameBuilder b) {
		Struct[] descendants = descendantsOf(struct, datamart);
		if(descendants.length == 0) return;
		b.add("superclass");
		b.add("descendant", Arrays.stream(descendants)
				.map(descendant -> new FrameBuilder("descendant").add("struct").add("name", descendant.name$()).toFrame())
				.toArray(Frame[]::new));
	}

	private Frame[] ancestorsOf(Entity entity) {
		List<Frame> ancestors = new ArrayList<>();
		Entity parent = entity.asExtensionOf().entity();
		while(parent != null) {
			ancestors.add(new FrameBuilder("ancestor", "entity").add("name", parent.name$()).toFrame());
			parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
		}
		return ancestors.toArray(Frame[]::new);
	}

	private Frame ancestorFrameOf(Entity entity) {
		return new FrameBuilder("ancestor", "entity").add("name", entity.name$()).toFrame();
	}

	private Frame ancestorFrameOf(Struct struct) {
		return new FrameBuilder("ancestor", "struct").add("name", struct.name$()).toFrame();
	}

//	private Frame[] ancestorsOf(Struct struct) {
//		List<Frame> ancestors = new ArrayList<>();
//		Struct parent = struct.asExtensionOf().struct();
//		while(parent != null) {
//			ancestors.add(new FrameBuilder("ancestor", "struct").add("name", parent.name$()).toFrame());
//			parent = parent.isExtensionOf() ? parent.asExtensionOf().struct() : null;
//		}
//		return ancestors.toArray(Frame[]::new);
//	}

	private Frame[] attributeFrames(List<ConceptAttribute> attributes) {
		return attributes.stream()
				.map(this::attributeFrameBuilder)
				.map(FrameBuilder::toFrame)
				.toArray(Frame[]::new);
	}

	private FrameBuilder attributeFrameBuilder(ConceptAttribute attr) {
		FrameBuilder b = new FrameBuilder("attribute");

		if(attr.isList() || attr.isSet()) {
			setAttribCollectionInfo(attr, b);
		} else if(attr.isMap()) {
			b.add("type", "java.util.Map").add("collection").add("parameterTypeName", "java.lang.String").add("parameterType", "java.lang.String");
			b.add("parameter", new FrameBuilder("parameter"));
		} else if(attr.isEntity()) {
			b.add("type", modelPackage + ".entities." + attr.asEntity().entity().name$());
		}  else if(attr.isStruct()) {
			b.add("type", modelPackage + ".entities." + attr.ownerFullName() + "." + attr.asStruct().name$());
		} else if(attr.isWord()) {
			b.add("type", modelPackage + ".entities." + attr.ownerFullName() + "." + attr.type());
		} else {
			b.add("type", attr.type());
		}

		b.add("name", attr.name$());

		if(attr.inherited()) b.add("inherited");

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
			parameterType = modelPackage + ".entities." + attr.ownerFullName() + "." + attr.asStruct().name$();
			parameterTypeName = attr.asStruct().name$();
		} else if(attr.isWord()) {
			parameterType = modelPackage + ".entities." + attr.owner().name() + "." + parameterType;
		}
		b.add("parameterType", parameterType);
		b.add("parameterTypeName", parameterTypeName);

		FrameBuilder param = new FrameBuilder("parameter");
		if(attr.isEntity()) param.add("entity");
		else if(attr.isStruct()) param.add("struct");
		else if(attr.isWord()) param.add("word");

		if(attr.isStruct()) {
			param.add("name", attr.ownerFullName() + STRUCT_INTERNAL_CLASS_SEP + attr.asStruct().name$());
		} else {
			param.add("name", parameterTypeName);
		}

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

	private String fullNameOf(Struct s) {
		return s.name$();
//		if(!s.isExtensionOf()) return s.name$();
//
//		List<String> names = new ArrayList<>(4);
//
//		Struct parent = s.asExtensionOf().struct();
//		while(parent != null) {
//			names.add(parent.name$());
//			parent = parent.isExtensionOf() ? parent.asExtensionOf().struct() : null;
//		}
//		names.add(s.name$());
//
//		Collections.reverse(names);
//		return String.join(".", names);
	}

	private Frame[] framesOfUpperLevelDescendants(Entity parent, Datamart datamart) {
		return Arrays.stream(upperLevelDescendantsOf(parent, datamart))
				.map(c -> new FrameBuilder("subclass")
						.add("package", modelPackage + ".master")
						.add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private Entity[] descendantsOf(Entity parent, Datamart datamart) {
		return datamart.entityList(e -> isDescendantOf(e, parent)).toArray(Entity[]::new);
	}

	private Struct[] descendantsOf(Struct parent, Datamart datamart) {
		return datamart.structList(e -> isDescendantOf(e, parent)).toArray(Struct[]::new);
	}

	private Entity[] upperLevelDescendantsOf(Entity parent, Datamart datamart) {
		List<Entity> upperLevelDescendants = new ArrayList<>();

		for (Entity entity : datamart.entityList(e -> isDescendantOf(e, parent) && !e.isAbstract())) {
			if(anAncestorOfThisEntityIsAlreadyPresent(entity, upperLevelDescendants)) continue;
			removeAnyDescendantOfThisEntityIfPresent(entity, upperLevelDescendants);
			upperLevelDescendants.add(entity);
		}

		return upperLevelDescendants.toArray(new Entity[0]);
	}

	private boolean anAncestorOfThisEntityIsAlreadyPresent(Entity entity, List<Entity> upperLevelDescendants) {
		return upperLevelDescendants.stream().anyMatch(ancestor -> isDescendantOf(entity, ancestor));
	}

	private void removeAnyDescendantOfThisEntityIfPresent(Entity ancestor, List<Entity> upperLevelDescendants) {
		upperLevelDescendants.removeIf(e -> isDescendantOf(e, ancestor));
	}

	private static boolean isDescendantOf(Entity node, Entity expectedParent) {
		if(!node.isExtensionOf()) return false;
		Entity parent = node.asExtensionOf().entity();
		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
	}

	private static boolean isDescendantOf(Struct node, Struct expectedParent) {
		return false;
//		if(!node.isExtensionOf()) return false;
//		Struct parent = node.asExtensionOf().struct();
//		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
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

	@Override
	public Datamart datamart() {
		return null;
	}

	@Override
	public String workingPackage() {
		return null;
	}

	public record TerminalInfo(Terminal terminal, String terminalPackage) { }

	private static class Templates {
		final Template datamart = customize(new DatamartTemplate());
		final Template entityBase = customize(new EntityBaseTemplate());
		final Template entity = append(customize(new EntityTemplate()), customize(new StructTemplate()), customize(new AttributesTemplate()));
		final Template entityMounter = customize(new EntityMounterTemplate());
		final Template struct = customize(new StructTemplate());
		final Template structBase = append(customize(new StructBaseTemplate()), customize(new AttributesTemplate()));

		private static Template append(Template t1, Template... others) {
			RuleSet rules = new RuleSet();
			addRulesOf(t1, rules);
			for(Template t : others) addRulesOf(t, rules);
			return new Template() {
				@Override
				protected RuleSet ruleSet() {
					return rules;
				}
			};
		}

		private static void addRulesOf(Template t, RuleSet rules) {
			try {
				Method method = t.getClass().getDeclaredMethod("ruleSet");
				method.setAccessible(true);
				RuleSet ruleSet = (RuleSet) method.invoke(t);
				ruleSet.forEach(rules::add);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
