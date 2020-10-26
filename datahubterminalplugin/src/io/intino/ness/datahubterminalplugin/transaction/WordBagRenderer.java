package io.intino.ness.datahubterminalplugin.transaction;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WordBagRenderer {
	private final WordBag wordBag;
	private final Configuration conf;
	private final File destination;
	private final List<File> resDirectories;
	private final String rootPackage;

	public WordBagRenderer(WordBag wordBag, Configuration conf, File destination, List<File> resDirectories, String rootPackage) {
		this.wordBag = wordBag;
		this.conf = conf;
		this.destination = destination;
		this.resDirectories = resDirectories;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = wordBagPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = process(wordBag);
		Commons.writeFrame(packageFolder, wordBag.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("wordBag", frame)));
	}

	private Frame process(WordBag wordBag) {
		FrameBuilder builder = new FrameBuilder("wordBag").
				add("name", wordBag.name$()).
				add("type", wordBag.isFromResource() ? String.class.getSimpleName() : wordBag.name$());
		if (wordBag.isFromResource()) {
			builder.add("resource").add("resource", resource(wordBag));
			List<WordBag.FromResource.Attribute> columnList = wordBag.asFromResource().attributeList();
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
		} else builder.add("word", words(wordBag));
		return builder.toFrame();
	}

	private boolean isPrimitive(Data.Type attribute) {
		Data data = attribute.asData();
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isReal();
	}

	private String resource(WordBag wordBag) {
		try {
			Path source = new File(wordBag.asFromResource().tsv().getPath()).getCanonicalFile().toPath();
			for (File resDirectory : resDirectories) {
				try {
					return resDirectory.toPath().relativize(source).toFile().getPath();
				} catch (IllegalArgumentException ex) {
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	private Frame[] words(WordBag wordBag) {
		return wordBag.asFromCode().wordList().stream().
				map(w -> new FrameBuilder("word").add("name", w.name$()).add("index", w.value()).add("labal", w.label()).toFrame()).toArray(Frame[]::new);
	}

	private Template template() {
		return Formatters.customize(new WordBagTemplate());
	}

	private String wordBagPackage() {
		return rootPackage + ".wordbag";
	}
}
