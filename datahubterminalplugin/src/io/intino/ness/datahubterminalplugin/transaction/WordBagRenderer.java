package io.intino.ness.datahubterminalplugin.transaction;

import io.intino.Configuration;
import io.intino.datahub.graph.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;

public class WordBagRenderer {
	private final WordBag wordBag;
	private final Configuration conf;
	private final File destination;
	private final String rootPackage;

	public WordBagRenderer(WordBag wordBag, Configuration conf, File destination, String rootPackage) {
		this.wordBag = wordBag;
		this.conf = conf;
		this.destination = destination;
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
		if (wordBag.isFromResource()) builder.add("resource").add("resource", resource(wordBag));
		else builder.add("word", words(wordBag));
		return builder.toFrame();
	}


	private String resource(WordBag wordBag) {
		String s = wordBag.asFromResource().tsv().toString();
		return conf.artifact().groupId().replace(".", "/") + "/ontology/" + new File(s).getName();
	}


	private String[] words(WordBag wordBag) {
		return wordBag.wordList().stream().
				map(w -> w.name$() + "(" + w.value() + ")").toArray(String[]::new);
	}

	private Template template() {
		return Formatters.customize(new WordBagTemplate());
	}

	private String wordBagPackage() {
		return rootPackage + ".wordbag";
	}
}
