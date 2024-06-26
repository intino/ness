package io.intino.ness.builder.codegeneration.datamarts;

import io.intino.builder.CompilerConfiguration;
import io.intino.datahub.model.*;
import io.intino.datahub.model.rules.SnapshotScale;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.ness.builder.IntinoException;
import io.intino.ness.builder.codegeneration.Formatters;
import io.intino.ness.builder.codegeneration.datamarts.nodes.IndicatorImplTemplate;
import io.intino.ness.builder.codegeneration.datamarts.nodes.ReelNodeImplTemplate;
import io.intino.ness.builder.codegeneration.datamarts.nodes.TimelineNodeImplTemplate;
import io.intino.ness.builder.util.ErrorUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;
import static java.io.File.separator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class DatamartsRenderer implements ConceptRenderer {
	private static final String DOT = ".";
	private static final String JAVA = ".java";

	private final File srcFolder;
	private final NessGraph model;
	private final CompilerConfiguration configuration;
	private String modelPackage;
	private final Templates templates;

	public DatamartsRenderer(File srcDir, NessGraph model, CompilerConfiguration configuration, String basePackage) {
		this.srcFolder = srcDir;
		this.model = model;
		this.configuration = configuration;
		srcFolder.mkdirs();
		this.modelPackage = basePackage;
		this.templates = new Templates();
	}

	public void render(Terminal terminal, String terminalPackage) throws IntinoException {
		TerminalInfo terminalInfo = new TerminalInfo(terminal, terminalPackage);
		String basePackage = modelPackage + ".datamarts";
		for (Datamart datamart : terminal.datamarts().list()) {
			this.modelPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderDatamart(datamart, terminalInfo);
		}
	}

	private void renderDatamart(Datamart datamart, TerminalInfo terminalInfo) throws IntinoException {
		try {
			write(renderImplementationOf(datamart, terminalInfo));
			write(entityMounterClassesOf(datamart, terminalInfo));
		} catch (Throwable e) {
			throw new IntinoException("Error during java className generation: " + ErrorUtils.getMessage(e));
		}
	}

	public void render() throws IntinoException {
		String basePackage = modelPackage + ".datamarts";
		for (Datamart datamart : model.datamartList()) {
			this.modelPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderDatamart(datamart);
			renderOntologyClassesOf(datamart);
		}
	}

	private void renderDatamart(Datamart datamart) throws IntinoException {
		try {
			write(renderInterfaceOf(datamart));
		} catch (Throwable e) {
			throw new IntinoException("Error during java className generation: " + ErrorUtils.getMessage(e));
		}
	}

	private void renderOntologyClassesOf(Datamart datamart) throws IntinoException {
		try {
			configuration.out().println(PRESENTABLE_MESSAGE + "nessc: Rendering entities and structs of " + datamart.name$() + "...");
			write(structClassesOf(datamart));
			write(entityClassesOf(datamart));
		} catch (Exception e) {
			throw new IntinoException("Error during java className generation: " + ErrorUtils.getMessage(e));
		}
	}

	private Map<String, String> entityMounterClassesOf(Datamart datamart, TerminalInfo terminalInfo) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(destination(baseEntityMounterName(datamart, terminalInfo)), templates.entityMounter.render(entityMounterInterface(datamart, terminalInfo)));
		datamart.entityList().stream().filter(e -> e.from() != null).forEach(e -> outputs.putAll(renderEntityMounter(e, datamart, terminalInfo)));
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
		return ".datamarts." + Formatters.javaValidName().format(datamart.name$().toLowerCase());
	}

	private Map<String, String> entityClassesOf(Datamart datamart) {
		Map<String, String> outputs = new HashMap<>();
		renderEntityBase(datamart, outputs);
		datamart.entityList().forEach(e -> outputs.putAll(renderEntity(e, datamart)));
		return outputs;
	}

	private void renderEntityBase(Datamart datamart, Map<String, String> outputs) {
		outputs.put(entityDestination(entityBaseName(datamart)), templates.entity.render(entityBaseBuilder(datamart)));
	}

	private String entityBaseName(Datamart datamart) {
		return modelPackage + "." + Formatters.javaValidName().format(firstUpperCase(datamart.name$()) + "Entity").toString();
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
		output.put(destination(structBaseName(datamart)), templates.struct.render(structBaseBuilder(datamart)));
	}

	private String structBaseName(Datamart datamart) {
		return modelPackage + "." + Formatters.javaValidName().format(firstUpperCase(datamart.name$()) + "Struct").toString();
	}

	private FrameBuilder structBaseBuilder(Datamart datamart) {
		return new FrameBuilder("struct", "base")
				.add("package", modelPackage)
				.add("datamart", datamart.name$());
	}

	private Map<String, String> renderInterfaceOf(Datamart datamart) {
		String theInterface = modelPackage + DOT + firstUpperCase(Formatters.javaValidName().format(datamart.name$() + "Datamart").toString());
		return Map.of(destination(theInterface), templates.datamart.render(datamartInterfaceBuilder(datamart).toFrame()));
	}

	private Map<String, String> renderImplementationOf(Datamart datamart, TerminalInfo terminalInfo) {
		String theImplementation = modelPackage + DOT + firstUpperCase(Formatters.javaValidName().format(datamart.name$() + "DatamartImpl").toString());
		return Map.of(destination(theImplementation), templates.datamartImpl.render(datamartImplBuilder(datamart, terminalInfo).toFrame()));
	}

	private FrameBuilder datamartInterfaceBuilder(Datamart datamart) {
		FrameBuilder builder = new FrameBuilder("datamart", "interface");
		builder.add("package", modelPackage);
		builder.add("name", firstUpperCase(datamart.name$()));
		builder.add("entity", entitiesOf(datamart));
		if (!datamart.timelineList().isEmpty()) {
			builder.add("hasTimelines", "");
			builder.add("timeline", timelinesOf(datamart));
			builder.add("indicator", indicatorsOf(datamart));
		}
		if (!datamart.reelList().isEmpty()) {
			builder.add("hasReels", "");
			builder.add("reel", reelsOf(datamart));
		}
		return builder;
	}

	private Frame reelNode(Datamart datamart) {
		FrameBuilder b = new FrameBuilder("reelNode", "default");
		b.add("datamart", datamart.name$());
		b.add("chronosObject", "Reel");
		return b.toFrame();
	}

	private String reelEvents(Datamart datamart) {
		return datamart.reelList().stream()
				.map(r -> Formatters.quoted().format(r.tank().message().name$()).toString())
				.distinct()
				.collect(joining(","));
	}

	private Frame[] reelsOf(Datamart datamart) {
		return datamart.reelList().stream().map(this::reelFrame).toArray(Frame[]::new);
	}

	private Frame reelFrame(Reel reel) {
		FrameBuilder b = new FrameBuilder("reel");
		b.add("package", modelPackage);
		b.add("name", reel.tank().message().name$());
		b.add("sources", sourcesOf(reel));
		b.add("entity", firstUpperCase(reel.entity().name$()));
		return b.toFrame();
	}

	private String sourcesOf(Reel reel) {
		ArrayList<String> sources = new ArrayList<>(List.of(Formatters.quoted().format(reel.tank()).toString()));
		if (reel.entity() != null && reel.entity().from() != null)
			sources.add(Formatters.quoted().format(reel.entity().from().message().name$()).toString());
		return String.join(",", sources);
	}

	private Frame timelineNode(Datamart datamart) {
		FrameBuilder b = new FrameBuilder("timelineNode", "default");
		b.add("datamart", datamart.name$());
		b.add("chronosObject", "Timeline");
		return b.toFrame();
	}

	private Frame indicatorNode(Datamart datamart) {
		FrameBuilder b = new FrameBuilder("indicatorNode", "default");
		b.add("datamart", datamart.name$());
		b.add("chronosObject", "Indicator");
		return b.toFrame();
	}

	private String timelineEvents(Datamart datamart) {
		return datamart.timelineList().stream()
				.flatMap(TimelineUtils::types)
				.distinct()
				.map(t -> Formatters.quoted().format(t).toString())
				.collect(joining(","));
	}

	private Frame[] indicatorsOf(Datamart datamart) {
		Stream<Frame> cooked = datamart.timelineList().stream()
				.filter(Timeline::isCooked)
				.filter(Timeline::isIndicator)
				.map(Timeline::asCooked)
				.flatMap(this::indicatorFrames);
		Stream<Frame> raw = datamart.timelineList().stream()
				.filter(Timeline::isRaw)
				.filter(Timeline::isIndicator)
				.map(Timeline::asRaw)
				.flatMap(this::indicatorFrames);
		return Stream.concat(cooked, raw).toArray(Frame[]::new);
	}

	private Frame[] timelinesOf(Datamart datamart) {
		List<Frame> frames = new ArrayList<>();
		frames.addAll(datamart.timelineList().stream().filter(Timeline::isRaw).map(Timeline::asRaw).map(this::timelineFrame).toList());
		frames.addAll(datamart.timelineList().stream().filter(Timeline::isCooked).map(Timeline::asCooked).map(this::timelineFrame).toList());
		return frames.toArray(new Frame[0]);
	}

	private Stream<Frame> indicatorFrames(Timeline.Cooked timeline) {
		return timeline.timeSeriesList().stream()
				.map(Layer::name$)
				.map(ts -> new FrameBuilder("indicator")
						.add("package", modelPackage)
						.add("label", Formatters.firstLowerCase(ts))
						.add("name", timeline.name$() + "." + ts)
						.add("sources", TimelineUtils.types(timeline.asTimeline()).map(t -> Formatters.quoted().format(t).toString()).collect(joining(",")))
						.add("entity", firstUpperCase(timeline.entity().name$())).toFrame());
	}

	private Stream<Frame> indicatorFrames(Timeline.Raw timeline) {
		return timeline.tank().sensor().magnitudeList().stream().map(Layer::name$)
				.map(ts -> new FrameBuilder("indicator")
						.add("package", modelPackage)
						.add("label", Formatters.firstLowerCase(ts))
						.add("name", timeline.tank().asTank().asMeasurement().sensor().name$() + "." + ts)
						.add("sources", TimelineUtils.types(timeline.asTimeline()).map(t -> Formatters.quoted().format(t).toString()).collect(joining(",")))
						.add("entity", firstUpperCase(timeline.entity().name$())).toFrame());
	}

	private Frame timelineFrame(Timeline.Raw timeline) {
		FrameBuilder b = new FrameBuilder("timeline", "raw");
		b.add("package", modelPackage);
		b.add("name", timeline.tank().sensor().name$());
		b.add("sources", sourcesOf(timeline));
		b.add("entity", firstUpperCase(timeline.entity().name$()));
		return b.toFrame();
	}

	private Frame timelineFrame(Timeline.Cooked timeline) {
		FrameBuilder b = new FrameBuilder("timeline", "cooked");
		b.add("package", modelPackage);
		b.add("name", timeline.name$());
		b.add("sources", TimelineUtils.types(timeline.asTimeline()).map(t -> Formatters.quoted().format(t).toString()).collect(joining(",")));
		b.add("entity", firstUpperCase(timeline.entity().name$()));
		return b.toFrame();
	}

	private String sourcesOf(Timeline.Raw timeline) {
		String sensor = Formatters.quoted().format(timeline.tank().sensor().name$()).toString();
		if (timeline.entity() != null && timeline.entity().from() != null)
			return sensor + "," + Formatters.quoted().format(timeline.entity().from().message().name$()).toString();
		return sensor;
	}

	private FrameBuilder datamartImplBuilder(Datamart datamart, TerminalInfo terminalInfo) {
		FrameBuilder builder = new FrameBuilder("datamart", "message", "impl");
		builder.add("package", terminalInfo.terminalPackage + subPackageOf(datamart));
		Datamart.Snapshots snapshots = datamart.snapshots();
		builder.add("name", datamart.name$()).add("scale", (snapshots == null || snapshots.scale() == null) ? SnapshotScale.None.name() : snapshots.scale().name());
		builder.add("entity", entitiesOf(datamart));
		builder.add("struct", structsOf(datamart));
		builder.add("numEntities", datamart.entityList().size());
		builder.add("numStructs", datamart.structList().size());
		builder.add("ontologypackage", modelPackage);
		builder.add("terminal", String.format(terminalInfo.terminalPackage + "." + firstUpperCase(Formatters.javaValidName().format(terminalInfo.terminal.name$()).toString())));
		builder.add("lineSeparator", "\n");

		if (!datamart.timelineList().isEmpty()) {
			builder.add("hasTimelines", "");
			builder.add("timelineEvents", timelineEvents(datamart));
			builder.add("timeline", timelinesOf(datamart));
			builder.add("timelineNode", timelineNode(datamart));
			builder.add("indicator", indicatorsOf(datamart));
			builder.add("indicatorNode", indicatorNode(datamart));
		}
		if (!datamart.reelList().isEmpty()) {
			builder.add("hasReels", "");
			builder.add("reelEvents", reelEvents(datamart));
			builder.add("reel", reelsOf(datamart));
			builder.add("reelNode", reelNode(datamart));
		}
		return builder;
	}

	private Frame[] structsOf(Datamart datamart) {
		List<Frame> structFrames = datamart.structList().stream()
				.flatMap(struct -> framesOf(struct, modelPackage + ".structs", null))
				.collect(Collectors.toList());

		for (Entity entity : datamart.entityList()) {
			entity.structList().stream()
					.flatMap(struct -> framesOf(struct, modelPackage + ".entities." + entity.name$(), entity.name$()))
					.forEach(structFrames::add);
		}

		return structFrames.toArray(Frame[]::new);
	}

	private Stream<Frame> framesOf(Struct struct, String thePackage, String owner) {
		String fullname = owner == null ? fullNameOf(struct) : owner + StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP + fullNameOf(struct);
		List<ConceptAttribute> attributes = attributesOf(struct);
		attributes.forEach(a -> a.ownerFullName(fullname));
		FrameBuilder b = new FrameBuilder("struct");
		b.add("package", thePackage);
		b.add("name", firstUpperCase(struct.name$()));
		b.add("fullName", fullname);
		b.add("attribute", attributeFrames(attributes));
		List<Frame> frames = new ArrayList<>(1);
		frames.add(b.toFrame());
		for (Struct s : struct.structList())
			framesOf(s, thePackage + "." + struct.name$(), fullname).forEach(frames::add);

		return frames.stream();
	}

	private Frame[] entitiesOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(entity -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage);
					b.add("name", firstUpperCase(entity.name$())).add("fullName", fullNameOf(entity));
					b.add("datamart", datamart.name$());
					b.add("attribute", attributeFrames(attributesOf(entity)));
					if (entity.from() != null) b.add("event", firstUpperCase(entity.from().message().name$()));
					if (entity.isExtensionOf()) {
						b.add("parent", entity.asExtensionOf().entity().name$());
						b.add("ancestor", ancestorsOf(entity));
					} else b.add("hasNoParents", "true");
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
		struct.structList().stream().map(s -> attrOf(struct.core$(), s)).filter(a -> !attributes.contains(a)).forEach(attributes::add);
		return attributes;
	}

	private void setDescendantsInfo(Datamart datamart, Entity entity, FrameBuilder b) {
		Entity[] descendants = descendantsOf(entity, datamart);
		if (descendants.length == 0) return;
		b.add("superclass");
		b.add("descendant", Arrays.stream(descendants).map(e -> new FrameBuilder("descendant").add("entity").add("name", e.name$()).toFrame()).toArray(Frame[]::new));
		b.add("subclasstop", framesOfNonAbstractTopLevelDescendants(entity, datamart));
		b.add("subclass", framesOfNonAbstractDescendants(entity, datamart));
	}

	private Frame[] ancestorsOf(Entity entity) {
		List<Frame> ancestors = new ArrayList<>();
		Entity parent = entity.asExtensionOf().entity();
		while (parent != null) {
			ancestors.add(new FrameBuilder("ancestor", "entity").add("name", parent.name$()).toFrame());
			parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
		}
		return ancestors.toArray(Frame[]::new);
	}

	private Frame[] attributeFrames(List<ConceptAttribute> attributes) {
		return attributes.stream()
				.map(this::attributeFrameBuilder)
				.map(FrameBuilder::toFrame)
				.toArray(Frame[]::new);
	}

	private FrameBuilder attributeFrameBuilder(ConceptAttribute attr) {
		FrameBuilder b = new FrameBuilder("attribute");

		if (attr.isList() || attr.isSet()) {
			setAttribCollectionInfo(attr, b);
		} else if (attr.isMap()) {
			b.add("type", "java.util.Map").add("collection").add("parameterTypeName", "java.lang.String").add("parameterType", "java.lang.String");
			b.add("parameter", new FrameBuilder("parameter"));
		} else if (attr.isEntity()) {
			b.add("type", modelPackage + ".entities." + attr.asEntity().entity().name$());
		} else if (attr.isStruct()) {
			b.add("type", modelPackage + ".entities." + attr.ownerFullName().replace(StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP, ".") + "." + attr.asStruct().name$());
		} else if (attr.isWord()) {
			b.add("type", modelPackage + ".entities." + attr.ownerFullName().replace(StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP, ".") + "." + attr.type());
		} else {
			b.add("type", attr.type());
		}

		b.add("name", attr.name$());

		if (attr.inherited()) b.add("inherited");

		return b;
	}

	private void setAttribCollectionInfo(ConceptAttribute attr, FrameBuilder b) {
		b.add("type", attr.isList() ? "java.util.List" : "java.util.Set");
		b.add("collection");
		String parameterType = attr.type();
		String parameterTypeName = parameterType;
		if (attr.isEntity()) {
			parameterType = modelPackage + ".entities." + firstUpperCase(attr.asEntity().entity().name$());
			parameterTypeName = firstUpperCase(attr.asEntity().entity().name$());
		} else if (attr.isStruct()) {
			parameterType = modelPackage + ".entities." + attr.ownerFullName() + "." + firstUpperCase(attr.asStruct().name$());
			parameterTypeName = firstUpperCase(attr.asStruct().name$());
		} else if (attr.isWord()) {
			parameterType = modelPackage + ".entities." + firstUpperCase(attr.owner().name()) + "." + parameterType;
		}
		b.add("parameterType", parameterType.replace(StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP, "."));
		b.add("parameterTypeName", parameterTypeName);

		FrameBuilder param = new FrameBuilder("parameter");
		if (attr.isEntity()) param.add("entity");
		else if (attr.isStruct()) param.add("struct");
		else if (attr.isWord()) param.add("word");

		if (attr.isStruct()) {
			param.add("name", attr.ownerFullName() + StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP + firstUpperCase(attr.asStruct().name$()));
		} else {
			param.add("name", parameterTypeName);
		}

		b.add("parameter", param);
	}

	private String firstUpperCase(String name) {
		return Formatters.firstUpperCase(name);
	}

	private String fullNameOf(Entity e) {
		if (!e.isExtensionOf()) return firstUpperCase(e.name$());
		List<String> names = new ArrayList<>(4);
		Entity parent = e.asExtensionOf().entity();
		while (parent != null) {
			names.add(firstUpperCase(parent.name$()));
			parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
		}
		Collections.reverse(names);
		names.add(firstUpperCase(e.name$()));
		return String.join(".", names);
	}

	private String fullNameOf(Struct s) {
		return firstUpperCase(s.name$());
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

	private Frame[] framesOfNonAbstractTopLevelDescendants(Entity parent, Datamart datamart) {
		return Arrays.stream(upperLevelDescendantsOf(parent, datamart))
				.map(c -> new FrameBuilder("subclasstop")
						.add("package", modelPackage + ".entities")
						.add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private Frame[] framesOfNonAbstractDescendants(Entity parent, Datamart datamart) {
		return Arrays.stream(nonAbstractDescendants(parent, datamart))
				.map(c -> new FrameBuilder("subclass")
						.add("package", modelPackage + ".entities")
						.add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private Entity[] descendantsOf(Entity parent, Datamart datamart) {
		return datamart.entityList(e -> isDescendantOf(e, parent)).toArray(Entity[]::new);
	}

	private Entity[] nonAbstractDescendants(Entity parent, Datamart datamart) {
		return datamart.entityList(e -> !e.isAbstract() && isDescendantOf(e, parent)).toArray(Entity[]::new);
	}

	private Entity[] upperLevelDescendantsOf(Entity parent, Datamart datamart) {
		List<Entity> upperLevelDescendants = new ArrayList<>();

		for (Entity entity : datamart.entityList(e -> isDescendantOf(e, parent) && !e.isAbstract())) {
			if (anAncestorOfThisEntityIsAlreadyPresent(entity, upperLevelDescendants)) continue;
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
		if (!node.isExtensionOf()) return false;
		Entity parent = node.asExtensionOf().entity();
		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
	}

	private Map<String, String> renderEntity(Entity entity, Datamart datamart) {
		return new EntityFrameFactory(modelPackage, datamart).create(entity).entrySet().stream()
				.collect(toMap(
						e -> entityDestination(e.getKey()),
						e -> templates.entityImpl.render(e.getValue()))
				);
	}

	private Map<String, String> renderStruct(Datamart datamart, Struct struct) {
		return new StructFrameFactory(datamart, modelPackage).create(struct).entrySet().stream()
				.collect(toMap(
						e -> destination(e.getKey()),
						e -> templates.structImpl.render(e.getValue()))
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

	private void write(Map<String, String> outputsMap) throws IntinoException {
		for (Map.Entry<String, String> entry : outputsMap.entrySet()) {
			File file = new File(entry.getKey());
			if (entry.getValue().isEmpty() || isUnderSource(file) && file.exists()) continue;
			file.getParentFile().mkdirs();
			write(file, entry.getValue());
		}
	}

	private boolean isUnderSource(File file) {
		return file.getAbsolutePath().startsWith(srcFolder.getAbsolutePath());
	}

	private void write(File file, String text) throws IntinoException {
		try {
			file.getParentFile().mkdirs();
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
			fileWriter.write(text);
			fileWriter.close();
		} catch (IOException e) {
			throw new IntinoException(ErrorUtils.getMessage(e));
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

	public record TerminalInfo(Terminal terminal, String terminalPackage) {
	}

	private static class Templates {
		final Template datamart = append(Formatters.customize(new DatamartTemplate()));
		final Template datamartImpl = append(Formatters.customize(new DatamartImplTemplate()), Formatters.customize(new IndicatorImplTemplate()), Formatters.customize(new ReelNodeImplTemplate()), Formatters.customize(new TimelineNodeImplTemplate()));
		final Template entity = Formatters.customize(new EntityTemplate());
		final Template entityImpl = append(Formatters.customize(new EntityImplTemplate()), Formatters.customize(new StructImplTemplate()), Formatters.customize(new AttributesTemplate()));
		final Template entityMounter = Formatters.customize(new EntityMounterTemplate());
		final Template structImpl = Formatters.customize(new StructImplTemplate());
		final Template struct = append(Formatters.customize(new StructTemplate()), Formatters.customize(new AttributesTemplate()));

		private static Template append(Template t1, Template... others) {
			RuleSet rules = new RuleSet();
			addRulesOf(t1, rules);
			for (Template t : others) addRulesOf(t, rules);
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
