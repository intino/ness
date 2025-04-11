package systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts;

import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.Engine;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.template.Template;
import systems.intino.eventsourcing.datahub.model.*;
import systems.intino.eventsourcing.datahub.model.rules.SnapshotScale;
import systems.intino.eventsourcing.datahubbuilder.IntinoException;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters;
import systems.intino.eventsourcing.datahubbuilder.util.ErrorUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;
import static java.io.File.separator;
import static java.util.stream.Collectors.toMap;

public class DatamartsRenderer {
	private static final String DOT = ".";
	private static final String JAVA = ".java";
	private final File srcFolder;
	private final NessGraph model;
	private final CompilerConfiguration configuration;
	private final Templates templates;
	private final String basePackage;
	private String datamartPackage;

	public DatamartsRenderer(File srcDir, NessGraph model, CompilerConfiguration configuration, String basePackage) {
		this.srcFolder = srcDir;
		this.model = model;
		this.configuration = configuration;
		this.datamartPackage = basePackage;
		this.basePackage = datamartPackage + ".datamarts";
		this.srcFolder.mkdirs();
		this.templates = new Templates();
	}

	public void render() throws IntinoException {
		for (Datamart datamart : model.datamartList()) {
			this.datamartPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderOntologyClassesOf(datamart);
		}
	}

	public void render(Terminal terminal, String terminalPackage) throws IntinoException {
		for (Datamart datamart : terminal.datamarts().list()) {
			this.datamartPackage = basePackage + "." + datamart.name$().toLowerCase();
			renderDatamart(datamart, new TerminalInfo(terminal, terminalPackage));
		}
	}

	private void renderDatamart(Datamart datamart, TerminalInfo terminalInfo) throws IntinoException {
		try {
			write(render(datamart, terminalInfo));
			write(subjectMounterClassesOf(datamart, terminalInfo));
		} catch (Throwable e) {
			throw new IntinoException("Error during java className generation: " + ErrorUtils.getMessage(e));
		}
	}

	private void renderOntologyClassesOf(Datamart datamart) throws IntinoException {
		try {
			configuration.out().println(PRESENTABLE_MESSAGE + "nessc: Rendering entities and structs of " + datamart.name$() + "...");
			write(renderSubjects(datamart));
		} catch (Exception e) {
			throw new IntinoException("Error during java className generation: " + ErrorUtils.getMessage(e));
		}
	}

	private Map<String, String> subjectMounterClassesOf(Datamart datamart, TerminalInfo terminalInfo) {
		Map<String, String> outputs = new HashMap<>();
		outputs.put(destination(baseSubjectMounterName(datamart, terminalInfo)), engine(templates.subjectMounter).render(subjectMounter(datamart, terminalInfo)));
		datamart.subjectList().stream().filter(e -> e.from() != null).forEach(e -> outputs.putAll(renderSubjectMounter(e, datamart, terminalInfo)));
		return outputs;
	}

	private String baseSubjectMounterName(Datamart datamart, TerminalInfo terminalInfo) {
		return terminalInfo.terminalPackage + "." + subPackageOf(datamart) + "." + firstUpperCase(datamart.name$()) + "Mounter";
	}

	private FrameBuilder subjectMounter(Datamart datamart, TerminalInfo terminalInfo) {
		return new FrameBuilder("mounter", "interface")
				.add("package", terminalInfo.terminalPackage + subPackageOf(datamart))
				.add("ontologypackage", datamartPackage)
				.add("datamart", datamart.name$());
	}

	private String subPackageOf(Datamart datamart) {
		return ".datamarts." + Formatters.javaValidName().format(datamart.name$().toLowerCase());
	}

	private Map<String, String> renderSubjects(Datamart datamart) {
		return Arrays.stream(framesOf(datamart))
				.collect(toMap(frame -> subjectDestination(calculateSubjectPath(frame)),
						frame -> engine(templates.subject).render(frame), (a, b) -> b));
	}

	private String calculateSubjectPath(Frame subject) {
		return String.join(DOT, datamartPackage, "subjects", firstUpperCase(Formatters.javaValidName().format(subject.frames("name").next().value()).toString()));
	}


	private Map<String, String> render(Datamart datamart, TerminalInfo terminalInfo) {
		String theImplementation = datamartPackage + DOT + firstUpperCase(Formatters.javaValidName().format(datamart.name$() + "Datamart").toString());
		return Map.of(destination(theImplementation), Formatters.customize(new Engine(templates.datamart)).render(datamartBuilder(datamart, terminalInfo).toFrame()));
	}

	private FrameBuilder datamartBuilder(Datamart datamart, TerminalInfo terminalInfo) {
		FrameBuilder builder = new FrameBuilder("datamart");
		builder.add("package", terminalInfo.terminalPackage + subPackageOf(datamart));
		Datamart.Snapshots snapshots = datamart.snapshots();
		builder.add("name", datamart.name$()).add("scale", (snapshots == null || snapshots.scale() == null) ? SnapshotScale.None.name() : snapshots.scale().name());
		builder.add("subject", framesOf(datamart));
//		builder.add("struct", structsOf(datamart));
		builder.add("ontologypackage", datamartPackage);
		builder.add("terminal", String.format(terminalInfo.terminalPackage + "." + firstUpperCase(Formatters.javaValidName().format(terminalInfo.terminal.name$()).toString())));
		return builder;
	}

//	private Frame[] structsOf(Datamart datamart) {
//		List<Frame> structFrames = datamart.structList().stream()
//				.flatMap(struct -> frameOf(struct, datamartPackage + ".structs", null))
//				.collect(Collectors.toList());
//		for (Subject subject : datamart.subjectList()) {
//			subject.structList().stream()
//					.flatMap(struct -> frameOf(struct, datamartPackage + ".subjects." + subject.name$(), subject.name$()))
//					.forEach(structFrames::add);
//		}
//		return structFrames.toArray(Frame[]::new);
//	}

	private Frame[] framesOf(Datamart datamart) {
		return datamart.subjectList().stream()
				.map(subject -> frameOf(datamart, subject))
				.toArray(Frame[]::new);
	}

	private Frame frameOf(Datamart datamart, Subject subject) {
		final FrameBuilder b = new FrameBuilder("subject", "root");
		b.add("package", datamartPackage);
		b.add("name", firstUpperCase(subject.name$()));
		b.add("datamart", datamart.name$());
		b.add("attribute", Utils.attributeFramesOf(subject));
		if (subject.isExtensionOf())
			b.add("parent", withFullPackage(subject.asExtensionOf().subject().name$()));
		b.add("descendant", datamart.subjectList().stream().filter(s -> !s.equals(subject) && isDescendantOf(s, subject))
				.map(s -> firstUpperCase(s.name$()))
				.toArray(String[]::new));
		if (subject.isAbstract()) b.add("abstract").add("isAbstract", subject.isAbstract());
		b.add("struct", subject.includeList().stream()
				.filter(Subject.Include::isStruct)
				.map(s -> frameOf(s.asStruct().messageComponent(), datamartPackage, null))
				.toArray(Frame[]::new));
		return b.toFrame();
	}

	private static boolean isDescendantOf(Subject node, Subject expectedParent) {
		if (!node.isExtensionOf()) return false;
		Subject parent = node.asExtensionOf().subject();
		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
	}

	private Frame frameOf(Component component, String thePackage, String owner) {
		String fullname = owner == null ? fullNameOf(component) : owner + StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP + fullNameOf(component);
		FrameBuilder b = new FrameBuilder("struct");
		b.add("package", thePackage);
		if (component.core$().owner().is(Component.class)) {
			b.add("isStruct", true).add("path", fullname);
		}
		if (component.multiple())
			b.add("multiple").add("multiple", new FrameBuilder().add("name", firstUpperCase(component.name$())));
		b.add("name", firstUpperCase(component.name$()));
		b.add("attribute", Utils.attributeFramesOf(component.attributeList()));
		b.add("struct", component.componentList().stream().map(s -> frameOf(s, thePackage + "." + component.name$(), fullname)).toArray(Frame[]::new));
		return b.toFrame();
	}

	private String withFullPackage(String parent) {
		return subjectsPackage() + parent;
	}

	private String subjectsPackage() {
		return datamartPackage + ".subjects.";
	}


	private String firstUpperCase(String name) {
		return Formatters.firstUpperCase(name);
	}


	private String fullNameOf(Component s) {
		return firstUpperCase(s.name$());
	}


	private Map<String, String> renderStruct(Datamart datamart, Component struct) {
		return new StructFrameFactory(datamart, datamartPackage).create(struct).entrySet().stream()
				.collect(toMap(
						e -> destination(e.getKey()),
						e -> engine(templates.struct).render(e.getValue()))
				);
	}

	private Map<String, String> renderSubjectMounter(Subject subject, Datamart datamart, TerminalInfo terminalInfo) {
		return new SubjectMounterFrameFactory(terminalInfo.terminalPackage + subPackageOf(datamart), datamartPackage, datamart)
				.create(subject).entrySet().stream()
				.collect(toMap(
						e -> subjectDestination(e.getKey()),
						e -> engine(templates.subjectMounter).render(e.getValue()))
				);
	}

	private Engine engine(Template template) {
		return Formatters.customize(new Engine(template));
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

	private String subjectDestination(String path) {
		return new File(srcFolder, path.replace(DOT, separator) + JAVA).getAbsolutePath();
	}

	public record TerminalInfo(Terminal terminal, String terminalPackage) {
	}

	private static class Templates {
		final Template datamart = new DatamartTemplate();
		final Template subject = new SubjectTemplate();
		final Template subjectMounter = new SubjectMounterTemplate();
		final Template struct = new StructTemplate();
	}
}
