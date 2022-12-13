package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.ness.datahubterminalplugin.util.ErrorUtils;
import io.intino.plugin.PluginLauncher;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.customize;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;
import static java.io.File.separator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

public class MasterRenderer {
	private static final String DOT = ".";
	private static final String JAVA = ".java";

	private final File srcFolder;
	private final NessGraph model;
	private final Configuration conf;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final Template entityTemplate;
	private final Template validatorTemplate;
	private final Template structTemplate;
	private String modelPackage;

	public MasterRenderer(File srcDir, NessGraph model, Configuration conf, PrintStream logger, PluginLauncher.Notifier notifier, String basePackage) {
		this.srcFolder = srcDir;
		this.model = model;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		this.entityTemplate = customize(new EntityTemplate());
		this.validatorTemplate = customize(new ValidatorTemplate());
		this.structTemplate = customize(new StructTemplate());
		srcFolder.mkdirs();
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
			if (model.entityList().isEmpty()) return false;
			write(entitiesInterfaces());
			write(validationLayerClass());
			return true;
		} catch (Throwable e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
			return false;
		}
	}

	public boolean renderOntologyClasses() {
		try {
			logger.println("Generating Entities...");
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
		model.entityList().forEach(e -> outputs.putAll(renderEntityAndValidator(e)));
		return outputs;
	}

	private Map<String, String> renderEntityAndValidator(Entity entity) {
		Map<String, String> map = renderEntityNode(entity);
		map.putAll(renderValidator(entity));
		return map;
	}

	private Map<String, String> structClasses() {
		return model.structList().stream()
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
								.add("entity", entities(""))
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
		builder.add("entity", entities(type));
		builder.add(type);
		return builder;
	}

	private Frame[] entities(String type, Terminal terminal) {
		Set<String> subscribeEntities = getSubscribeEntities(terminal);
		Set<String> publishEntities = terminal.publish().entityTanks().stream().map(e -> e.name$().toLowerCase()).collect(Collectors.toSet());
		return model.entityList().stream()
				.map(c -> {
					FrameBuilder b = new FrameBuilder("entity").add("name", c.name$());
					if (c.isAbstract()) {
						b.add("abstract");
						Frame[] subclasses = subclassesOf(c);
						if (subclasses.length > 0) b.add("subclass", subclasses);
					}
					b.add(type);
					if(subscribeEntities.contains(c.name$().toLowerCase())) b.add("subscribe");
					if(publishEntities.contains(c.name$().toLowerCase())) b.add("publish");
					return b.toFrame();
				}).toArray(Frame[]::new);
	}

	private Set<String> getSubscribeEntities(Terminal terminal) {
		Set<String> subscribeEntities = terminal.subscribe().entityTanks().stream().map(Layer::name$).map(String::toLowerCase).collect(Collectors.toSet());
		if(model.entityList().stream().map(e -> e.name$().toLowerCase()).allMatch(subscribeEntities::contains)) return subscribeEntities;
		resolveInterDependencies(subscribeEntities);
		return subscribeEntities;
	}

	private void resolveInterDependencies(Set<String> entities) {
		for(String name : entities.toArray(String[]::new)) {
			Entity entity = findEntity(name);
			if(entity == null) {
				Logger.warn("Entity " + name + " not found in model");
				continue;
			}
			getEntityReferencesOf(entity, entities);
		}
	}

	private void getEntityReferencesOf(Entity entity, Set<String> entities) {
		for(Entity.Attribute attribute : entity.attributeList()) {
			for(Entity ref : entityReferencesOf(attribute)) {
				if(entities.add(ref.name$().toLowerCase())) {
					getEntityReferencesOf(ref, entities);
				}
			}
		}
		for(Entity.Method method : entity.methodList()) {
			for(Entity ref : entityReferencesOf(method)) {
				if(entities.add(ref.name$().toLowerCase())) {
					getEntityReferencesOf(ref, entities);
				}
			}
		}
	}

	private Iterable<? extends Entity> entityReferencesOf(Entity.Method method) {
		if(method.isGetter()) return emptyList();
		if(method.isFunction()) {
			return entityReferencesOf(
					method.asFunction().returnType().asEntity(),
					method.asFunction().parameterList().stream()
							.filter(EntityData::isEntity)
							.map(EntityData::asEntity)
							.collect(Collectors.toList()));
		}
		if(method.isRoutine()) {
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
		if(returnType != null && returnType.entity() != null) entities.add(returnType.entity());
		params.stream().filter(Objects::nonNull).map(EntityData.Entity::entity).filter(Objects::nonNull).forEach(entities::add);
		return entities;
	}

	private Iterable<? extends Entity> entityReferencesOf(Entity.Attribute attribute) {
		EntityData.Entity e = attribute.asEntity();
		if(e != null && e.entity() != null) return List.of(e.entity());

		if(!attribute.isMap()) return emptyList();

		List<Entity> refs = new ArrayList<>(2);

		EntityData.Map map = attribute.asMap();

		EntityData.Map.Key key = map.key();
		EntityData.Entity ke = key.asEntity();
		if(ke != null && ke.entity() != null) refs.add(ke.entity());

		EntityData.Map.Value value = map.value();
		EntityData.Entity ve = value.asEntity();
		if(ve != null && ve.entity() != null) refs.add(ve.entity());

		return refs;
	}

	private Entity findEntity(String name) {
		return model.entityList(e -> name.equalsIgnoreCase(e.name$())).findFirst().orElse(null);
	}

	private Frame[] entities(String type) {
		return model.entityList().stream()
				.map(c -> {
					final FrameBuilder b = new FrameBuilder("entity").add("name", c.name$());
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
		return model.entityList().stream()
				.filter(e -> isSubclassOf(e, parent))
				.map(c -> new FrameBuilder("subclass").add("name", c.name$()).toFrame())
				.toArray(Frame[]::new);
	}

	private static boolean isSubclassOf(Entity node, Entity parent) {
		return node.isExtensionOf() && node.asExtensionOf().entity().equals(parent);//TODO ojo con las jerarquias. Aquí no se comprueba nada mas que la ascendencia directa
	}

	private Map<String, String> renderEntityNode(Entity entity) {
		return new EntityFrameCreator(modelPackage, model).create(entity).entrySet().stream()
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

	private String entityDestination(String path, Frame frame) {
		return new File(srcFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	private String validatorDestination(String path, Frame frame) {
		return new File(srcFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

}
