package io.intino.ness.terminal.builder.codegeneration.master;

import io.intino.alexandria.logger.Logger;
import io.intino.builder.CompilerConfiguration;
import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;
import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.terminal.builder.Formatters.customize;
import static io.intino.ness.terminal.builder.Formatters.javaValidName;
import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

public class MasterRenderer {
	private static final String DOT = ".";
	private static final String JAVA = ".java";

	private final Template entityTemplate;
	private final Template validatorTemplate;
	private final Template structTemplate;
	private final File srcDir;
	private final NessGraph graph;
	private final CompilerConfiguration configuration;
	private String modelPackage;

	public MasterRenderer(File srcDir, NessGraph graph, CompilerConfiguration configuration, String basePackage) {
		this.srcDir = srcDir;
		this.graph = graph;
		this.configuration = configuration;
		this.entityTemplate = customize(new EntityTemplate());
		this.validatorTemplate = customize(new ValidatorTemplate());
		this.structTemplate = customize(new StructTemplate());
		this.srcDir.mkdirs();
		this.modelPackage = basePackage;
	}

	public boolean renderOntology() {
		this.modelPackage += ".master";
		return renderMaster() && renderOntologyClasses();
	}

	public boolean renderTerminal(Terminal terminal) {
		try {
			write(entitiesImpl(terminal));
			return true;
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
	}

	public boolean renderMaster() {
		try {
			if (graph.entityList().isEmpty()) return false;
			write(entitiesInterfaces());
			write(validationLayerClass());
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	public boolean renderOntologyClasses() {
		try {
			configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Generating Entities...");
			write(structClasses());
			write(entityClasses());
			return true;
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
	}

	private Map<String, String> entityClasses() {
		Map<String, String> outputs = new HashMap<>();
		graph.entityList().forEach(e -> outputs.putAll(renderEntityAndValidator(e)));
		return outputs;
	}

	private Map<String, String> renderEntityAndValidator(Entity entity) {
		Map<String, String> map = renderEntityNode(entity);
		map.putAll(renderValidator(entity));
		return map;
	}

	private Map<String, String> structClasses() {
		return graph.structList().stream()
				.map(this::renderStructNode)
				.flatMap(e -> e.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<String, String> validationLayerClass() {
		String module = "General"; //javaValidName().format(firstUpperCase().format(terminalName)).toString();
		String qn = modelPackage + DOT + "validators" + DOT + module + "RecordValidationLayer";
		return Map.of(
				destination(qn), customize(new ValidatorTemplate()).render(
						new FrameBuilder("validationLayer", "class")
								.add("module", module)
								.add("entity", entitiesForInterfaces(""))
								.add("package", modelPackage + ".validators")
								.toFrame())
		);
	}

	private Map<String, String> entitiesInterfaces() {
		String masterView = modelPackage + DOT + firstUpperCase().format(javaValidName().format("EntitiesView").toString());
		String masterTerminal = modelPackage + DOT + firstUpperCase().format(javaValidName().format("Entities").toString());

		return Map.of(
				destination(masterView), customize(new EntitiesTemplate()).render(entitiesInterfaceFrameBuilder("view").toFrame()),
				destination(masterTerminal), customize(new EntitiesTemplate()).render(entitiesInterfaceFrameBuilder("interface").toFrame())
		);
	}

	private Map<String, String> entitiesImpl(Terminal terminal) {
		String cachedEntities = modelPackage + DOT + firstUpperCase().format(javaValidName().format("CachedEntities").toString());
		return Map.of(
				destination(cachedEntities), customize(new EntitiesTemplate()).render(entitiesImplFrameBuilder("cached", terminal).toFrame())
		);
	}

	private FrameBuilder entitiesImplFrameBuilder(String type, Terminal terminal) {
		FrameBuilder builder = new FrameBuilder("master").add("package", modelPackage);
		builder.add("entity", entities(type, terminal));
		builder.add(type);
		return builder;
	}

	private FrameBuilder entitiesInterfaceFrameBuilder(String type) {
		FrameBuilder builder = new FrameBuilder("master").add("package", modelPackage);
		builder.add("entity", entitiesForInterfaces(type));
		builder.add(type);
		return builder;
	}

	private Frame[] entities(String type, Terminal terminal) {
		Set<String> subscribeEntities = getSubscribeEntities(terminal);
		Set<String> publishEntities = terminal.publish() == null ? Set.of() : terminal.publish().entityTanks().stream().map(e -> e.name$().toLowerCase()).collect(Collectors.toSet());
		return graph.entityList().stream().map(c -> {
			FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage + ".master").add("name", c.name$());
			if (c.isAbstract()) {
				b.add("abstract");
				Frame[] subclasses = subclassesOf(c);
				if (subclasses.length > 0) b.add("subclass", subclasses);
			}
			b.add(type);
			if (findRecursivelyIn(c, subscribeEntities)) b.add("subscribe");
			if (findRecursivelyIn(c, publishEntities)) b.add("publish");
			return b.toFrame();
		}).toArray(Frame[]::new);
	}

	private boolean findRecursivelyIn(Entity entity, Set<String> entities) {
		if (entities.contains(entity.name$().toLowerCase())) return true;
		return anyOfItsParentsIsPresent(entity, entities);
	}

	private boolean anyOfItsParentsIsPresent(Entity entity, Set<String> entities) {
		if (!entity.isExtensionOf()) return false;
		Entity parent = entity.asExtensionOf().entity();
		if (parent == null) return false;
		if (entities.contains(parent.name$().toLowerCase())) return true;
		return anyOfItsParentsIsPresent(parent, entities);
	}

	private Set<String> getSubscribeEntities(Terminal terminal) {
		Terminal.Subscribe subscribe = terminal.subscribe();
		if (subscribe == null) return Set.of();
		Set<String> subscribeEntities = subscribe.entityTanks().stream().map(Layer::name$).map(String::toLowerCase).collect(Collectors.toSet());
		if (graph.entityList().stream().map(e -> e.name$().toLowerCase()).allMatch(subscribeEntities::contains))
			return subscribeEntities;
		resolveInterDependencies(subscribeEntities);
		return subscribeEntities;
	}

	private void resolveInterDependencies(Set<String> entities) {
		for (String name : entities.toArray(String[]::new)) {
			Entity entity = findEntity(name);
			if (entity == null) {
				Logger.warn("Entity " + name + " not found in model");
				continue;
			}
			getEntityReferencesOf(entity, entities);
		}
	}

	private void getEntityReferencesOf(Entity entity, Set<String> entities) {
		for (Entity.Attribute attribute : entity.attributeList()) {
			for (Entity ref : entityReferencesOf(attribute)) {
				if (entities.add(ref.name$().toLowerCase())) {
					getEntityReferencesOf(ref, entities);
				}
			}
		}
		for (Entity.Method method : entity.methodList()) {
			for (Entity ref : entityReferencesOf(method)) {
				if (entities.add(ref.name$().toLowerCase())) {
					getEntityReferencesOf(ref, entities);
				}
			}
		}
	}

	private Iterable<? extends Entity> entityReferencesOf(Entity.Method method) {
		if (method.isGetter()) return emptyList();
		if (method.isFunction()) {
			return entityReferencesOf(
					method.asFunction().returnType().asEntity(),
					method.asFunction().parameterList().stream()
							.filter(EntityData::isEntity)
							.map(EntityData::asEntity)
							.collect(Collectors.toList()));
		}
		if (method.isRoutine()) {
			return entityReferencesOf(
					null,
					method.asRoutine().parameterList().stream()
							.filter(EntityData::isEntity)
							.map(EntityData::asEntity)
							.collect(Collectors.toList()));
		}
		return emptyList();
	}

	private Iterable<? extends Entity> entityReferencesOf(EntityData.Entity returnType, List<EntityData.Entity> params) {
		List<Entity> entities = new ArrayList<>(1 + params.size());
		if (returnType != null && returnType.entity() != null) entities.add(returnType.entity());
		params.stream().filter(Objects::nonNull).map(EntityData.Entity::entity).filter(Objects::nonNull).forEach(entities::add);
		return entities;
	}

	private Iterable<? extends Entity> entityReferencesOf(Entity.Attribute attribute) {
		EntityData.Entity e = attribute.asEntity();
		if (e != null && e.entity() != null) return List.of(e.entity());
		return emptyList();
	}

	private Entity findEntity(String name) {
		return graph.entityList(e -> name.equalsIgnoreCase(e.name$())).findFirst().orElse(null);
	}

	private Frame[] entitiesForInterfaces(String type) {
		return graph.entityList().stream()
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("entity").add("package", modelPackage).add("name", c.name$());
					if (c.isAbstract()) {
						b.add("abstract");
						Frame[] subclasses = subclassesOf(c);
						if (subclasses.length > 0) b.add("subclass", subclasses);
					}
					b.add(type);
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Frame[] subclassesOf(Entity parent) {
		return graph.entityList().stream()
				.filter(e -> isSubclassOf(e, parent))
				.map(c -> new FrameBuilder("subclass").add("package", modelPackage + ".master")
						.add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private static boolean isSubclassOf(Entity node, Entity expectedParent) {
		if (!node.isExtensionOf()) return false;
		Entity parent = node.asExtensionOf().entity();
		return parent.equals(expectedParent) || isSubclassOf(parent, expectedParent);
	}

	private Map<String, String> renderEntityNode(Entity entity) {
		return new EntityFrameCreator(modelPackage, graph).create(entity).entrySet().stream()
				.collect(toMap(
						e -> entityDestination(e.getKey(), e.getValue()),
						e -> entityTemplate.render(e.getValue()))
				);
	}

	private Map<String, String> renderValidator(Entity entity) {
		return new ValidatorFrameCreator(modelPackage).create(entity).entrySet().stream()
				.collect(toMap(
						e -> validatorDestination(e.getKey(), e.getValue()),
						e -> validatorTemplate.render(e.getValue()))
				);
	}

	private Map<String, String> renderStructNode(Struct struct) {
		return new StructFrameCreator(modelPackage).create(struct).entrySet().stream()
				.collect(toMap(
						e -> destination(e.getKey()),
						e -> structTemplate.render(e.getValue()))
				);
	}

	private void write(Map<String, String> outputsMap) throws IOException {
		for (Map.Entry<String, String> entry : outputsMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			File file = new File(key);
			if (value.isEmpty() || isUnderSource(file) && file.exists()) continue;
			file.getParentFile().mkdirs();
			write(file, value);
		}
	}

	private boolean isUnderSource(File file) {
		return file.getAbsolutePath().startsWith(srcDir.getAbsolutePath());
	}

	private void write(File file, String text) throws IOException {
		file.getParentFile().mkdirs();
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		fileWriter.write(text);
		fileWriter.close();
	}

	private String destination(String path) {
		return new File(srcDir, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private String entityDestination(String path, Frame frame) {
		return new File(srcDir, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private String validatorDestination(String path, Frame frame) {
		return new File(srcDir, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

}
