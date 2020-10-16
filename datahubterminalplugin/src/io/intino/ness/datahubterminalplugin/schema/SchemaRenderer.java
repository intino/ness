package io.intino.ness.datahubterminalplugin.schema;

import io.intino.Configuration;
import io.intino.datahub.graph.*;
import io.intino.datahub.graph.Datalake.Split;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SchemaRenderer {
	private final Schema schema;
	private final Configuration conf;
	private final Split split;
	private final File destination;
	private final String rootPackage;

	public SchemaRenderer(Schema schema, Configuration conf, Split split, File destination, String rootPackage) {
		this.schema = schema;
		this.conf = conf;
		this.split = split;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = schemasPackage();
		if (schema.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + schema.core$().ownerAs(Namespace.class).qn();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createSchemaFrame(schema, rootPackage);
		Commons.writeFrame(packageFolder, schema.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("schema", frame)));
	}

	private Frame createSchemaFrame(Schema schema, String packageName) {
		FrameBuilder builder = new FrameBuilder("schema").
				add("name", schema.name$()).
				add("package", packageName).
				add("size", sizeOf(schema));

		builder.add("attribute", processAttributes(schema.attributeList(), schema.name$()));
		if (split != null) builder.add("split", new FrameBuilder().add("split").
				add("split", splits(split, split.isLeaf() ? Collections.singletonList(split) : split.leafs())));
		return builder.toFrame();
	}

	private int sizeOf(Schema schema) {
		return schema.attributeList().stream().map(a -> !a.isWordBag() ? a.asType().size() : sizeOf(a.asWordBag().wordBag())).reduce(Integer::sum).get();
	}

	private Integer sizeOf(WordBag wordBag) {
		try {
			return !wordBag.isFromResource() ? log2(wordBag.wordList().size()) : log2(countLines(wordBag));
		} catch (IOException e) {
			return 0;
		}
	}

	private int countLines(WordBag wordBag) throws IOException {
		return (int) new BufferedReader(new InputStreamReader(wordBag.asFromResource().tsv().openStream())).lines().count();
	}

	private FrameBuilder[] processAttributes(List<Attribute> attributes, String owner) {
		List<FrameBuilder> list = new ArrayList<>();
		int offset = 0;
		for (Attribute attribute : attributes) {
			FrameBuilder b = process(attribute, offset);
			if (b != null) {
				offset += attribute.isWordBag() ? sizeOf(attribute.asWordBag().wordBag()) : attribute.asType().size();
				list.add(b.add("owner", owner));
			}
		}
		return list.toArray(new FrameBuilder[0]);
	}

	private FrameBuilder process(Attribute attribute, int offset) {
		if (attribute.isWordBag()) return process(attribute.asWordBag().wordBag(), offset);
		else return processAttribute(attribute.asType(), offset);
	}

	private Frame[] splits(Split realSplit, List<Split> leafs) {
		List<Frame> frames = new ArrayList<>();
		if (!leafs.contains(realSplit) && !realSplit.label().isEmpty())
			frames.add(new FrameBuilder("enum").add("value", realSplit.qn().replace(".", "-")).toFrame());
		for (Split leaf : leafs) {
			FrameBuilder builder = new FrameBuilder("enum").add("value", leaf.qn().replace(".", "-")).add("qn", leaf.qn());
			frames.add(builder.toFrame());
		}
		return frames.toArray(new Frame[0]);
	}

	private FrameBuilder processAttribute(Data.Type attribute, int offset) {
		FrameBuilder builder = new FrameBuilder("attribute")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("bits", attribute.size())
				.add("offset", offset)
				.add("type", isPrimitive(attribute) ? attribute.primitive() : attribute.type());
		attribute.core$().conceptList().stream().filter(Concept::isAspect).map(Predicate::name).forEach(builder::add);
		return builder;
	}

	private FrameBuilder process(WordBag wordBag, int offset) {
		FrameBuilder builder = new FrameBuilder("attribute", "wordbag").
				add("name", wordBag.name$()).
				add("type", wordBag.isFromResource() ? String.class.getSimpleName() : wordBag.name$()).
				add("offset", offset).
				add("bits", sizeOf(wordBag));
		if (wordBag.isFromResource())
			builder.add("resource").add("resource", resource(wordBag));
		else builder.add("word", words(wordBag));
		return builder;
	}

	private String resource(WordBag wordBag) {
		String s = wordBag.asFromResource().tsv().toString();
		return conf.artifact().groupId().replace(".", "/") + "/ontology/" + new File(s).getName();
	}

	private boolean isPrimitive(Data.Type attribute) {
		Data data = attribute.asData();
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isReal();
	}

	private String[] words(WordBag wordBag) {
		return wordBag.wordList().stream().
				map(w -> w.name$() + "(" + w.value() + ")").toArray(String[]::new);
	}

	private String schemasPackage() {
		return rootPackage + ".schemas";
	}

	private Template template() {
		return Formatters.customize(new SchemaTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}

	public static int log2(int N) {
		return (int) (Math.log(N) / Math.log(2));
	}
}
