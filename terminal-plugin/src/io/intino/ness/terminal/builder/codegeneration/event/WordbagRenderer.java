package io.intino.ness.terminal.builder.codegeneration.event;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Data;
import io.intino.datahub.model.Wordbag;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.terminal.builder.Commons;
import io.intino.ness.terminal.builder.Formatters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class WordbagRenderer {
	private final Wordbag dimension;
	private final File destination;
	private final List<File> resDirectories;
	private final String rootPackage;

	public WordbagRenderer(Wordbag dimension, File destination, List<File> resDirectories, String rootPackage) {
		this.dimension = dimension;
		this.destination = destination;
		this.resDirectories = resDirectories;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = wordBagPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = process(dimension);
		Commons.writeFrame(packageFolder, dimension.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("dimension", frame)));
	}

	private Frame process(Wordbag dimension) {
		FrameBuilder builder = new FrameBuilder("wordbag").
				add("name", dimension.name$()).
				add("type", dimension.isInResource() ? String.class.getSimpleName() : dimension.name$());
		if (dimension.isInResource()) {
			builder.add("resource").add("resource", resource(dimension));
			List<Wordbag.InResource.Attribute> columnList = dimension.asInResource().attributeList();
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
		} else builder.add("category", words(dimension));
		return builder.toFrame();
	}

	private boolean isPrimitive(Data.Type attribute) {
		Data data = attribute.asData();
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isReal();
	}

	private String resource(Wordbag dimension) {
		try {
			Path source = new File(dimension.asInResource().tsv().getPath()).getCanonicalFile().toPath();
			for (File resDirectory : resDirectories) {
				try {
					return resDirectory.toPath().relativize(source).toFile().getPath().replace("\\","/");
				} catch (IllegalArgumentException ex) {
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	private Frame[] words(Wordbag dimension) {
		return dimension.asInline().wordList().stream().
				map(w -> new FrameBuilder("category").add("name", w.name$()).add("index", w.value()).add("label", w.label()).toFrame()).toArray(Frame[]::new);
	}

	private Template template() {
		return Formatters.customize(new WordbagTemplate());
	}

	private String wordBagPackage() {
		return rootPackage + ".dimension";
	}
}
