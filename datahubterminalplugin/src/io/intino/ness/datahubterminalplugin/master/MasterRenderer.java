package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
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
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;
import static java.io.File.separator;
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
	private final String workingPackage;

	public MasterRenderer(File root, NessGraph model, Configuration conf, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.srcFolder = new File(root, "src");
		this.model = model;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		this.entityTemplate = customize(new EntityTemplate());
		this.validatorTemplate = customize(new ValidatorTemplate());
		this.structTemplate = customize(new StructTemplate());
		srcFolder.mkdirs();
		workingPackage = this.conf.artifact().code().generationPackage();
	}

	public boolean render() {
		try {
			if (model.entityList().isEmpty()) return false;
			logger.println("Generating Entities...");
			write(entityClasses());
			write(structClasses());
			write(masterClass());
			write(validationLayerClass());
			return true;
		} catch (Throwable e) {
			notifier.notifyError("Error during java className generation: " + ErrorUtils.getMessage(e));
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
		String module = javaValidName().format(conf.artifact().name()).toString();
		String qn = workingPackage + DOT + "validators" + DOT + firstUpperCase().format(module) + "RecordValidationLayer";
		return Map.of(
				destination(qn), customize(new ValidatorTemplate()).render(
						new FrameBuilder("validationLayer", "class")
								.add("module", module)
								.add("entity", entities(""))
								.add("package", workingPackage + ".validators")
								.toFrame())
		);
	}

	private Map<String, String> masterClass() {
		String masterView = workingPackage + DOT + firstUpperCase().format(javaValidName().format("EntitiesView").toString());
		String fullLoad = workingPackage + DOT + firstUpperCase().format(javaValidName().format("CachedEntities").toString());
		String masterTerminal = workingPackage + DOT + firstUpperCase().format(javaValidName().format("Entities").toString());

		return Map.of(
				destination(masterView), customize(new EntitiesTemplate()).render(masterFrameBuilder("view").toFrame()),
				destination(fullLoad), customize(new EntitiesTemplate()).render(masterFrameBuilder("cached").toFrame()),
				destination(masterTerminal), customize(new EntitiesTemplate()).render(masterFrameBuilder("interface").toFrame())
		);
	}

	private FrameBuilder masterFrameBuilder(String type) {
		FrameBuilder builder = new FrameBuilder("master").add("package", workingPackage);
		builder.add("entity", entities(type));
		builder.add(type);
		return builder;
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
		return new EntityFrameCreator(workingPackage, model).create(entity).entrySet().stream()
				.collect(toMap(
						e -> entityDestination(e.getKey(), e.getValue()),
						e -> entityTemplate.render(e.getValue()))
				);
	}

	private Map<String, String> renderValidator(Entity entity) {
		return new ValidatorFrameCreator(workingPackage).create(entity).entrySet().stream()
				.collect(toMap(
						e -> validatorDestination(e.getKey(), e.getValue()),
						e -> validatorTemplate.render(e.getValue()))
				);
	}

	private Map<String, String> renderStructNode(Struct struct) {
		return new StructFrameCreator(workingPackage).create(struct).entrySet().stream()
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
