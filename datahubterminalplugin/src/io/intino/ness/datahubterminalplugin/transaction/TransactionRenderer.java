package io.intino.ness.datahubterminalplugin.transaction;

import io.intino.Configuration;
import io.intino.datahub.graph.*;
import io.intino.datahub.graph.Datalake.Split;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Layer;
import io.intino.magritte.framework.Predicate;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionRenderer {
	private final Transaction transaction;
	private final Configuration conf;
	private final Split split;
	private final File destination;
	private final String rootPackage;

	public TransactionRenderer(Transaction transaction, Configuration conf, Split split, File destination, String rootPackage) {
		this.transaction = transaction;
		this.conf = conf;
		this.split = split;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String transactionsPackage = transactionsPackage();
		if (transaction.core$().owner().is(Namespace.class))
			transactionsPackage = transactionsPackage + "." + transaction.core$().ownerAs(Namespace.class).qn();
		final File packageFolder = new File(destination, transactionsPackage.replace(".", File.separator));
		final Frame frame = createTransactionFrame(transaction, transactionsPackage);
		FrameBuilder builder = new FrameBuilder("root").add("root", transactionsPackage).add("package", transactionsPackage).add("transaction", frame);
		if (!transaction.graph().wordBagList().isEmpty()) builder.add("wordbagsImport", this.rootPackage);
		Commons.writeFrame(packageFolder, transaction.name$(), template().render(builder));
	}

	private Frame createTransactionFrame(Transaction transaction, String packageName) {
		FrameBuilder builder = new FrameBuilder("transaction").
				add("name", transaction.name$()).
				add("package", packageName).
				add("size", (int) Math.ceil(sizeOf(transaction) / (float) Byte.SIZE));
		if (transaction.attributeList().stream().noneMatch(a -> a.name$().equals("id")))
			builder.add("id", transaction.attributeList().stream().filter(Data::isId).map(Layer::name$).findFirst().orElse(null));
		builder.add("attribute", processAttributes(new ArrayList<>(transaction.attributeList()), transaction.name$()));
		if (split != null) {
			List<Split> leafs = split.isLeaf() ? Collections.singletonList(split) : split.leafs();
			builder.add("split", new FrameBuilder().add("split").add("enum", enums(split, leafs)));
		}
		return builder.toFrame();
	}

	private int sizeOf(Transaction transaction) {
		return transaction.attributeList().stream().map(a -> !a.isWordBag() ? a.asType().size() : sizeOf(a.asWordBag().wordBag())).reduce(Integer::sum).get();
	}

	private FrameBuilder[] processAttributes(List<Attribute> attributes, String owner) {
		List<FrameBuilder> list = new ArrayList<>();
		int offset = 0;
		attributes.sort(Comparator.comparingInt(a -> a.asType().size()));
		Collections.reverse(attributes);
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
		if (attribute.isWordBag()) return process(attribute.asWordBag().wordBag(), attribute.name$(), offset);
		else return processAttribute(attribute.asType(), offset);
	}

	private FrameBuilder processAttribute(Data.Type attribute, int offset) {
		FrameBuilder builder = new FrameBuilder("attribute")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("offset", offset)
				.add("type", isPrimitive(attribute) ? attribute.primitive() : attribute.type());
		attribute.core$().conceptList().stream().filter(Concept::isAspect).map(Predicate::name).forEach(builder::add);
		if (isAligned(attribute, offset)) builder.add("aligned", "Aligned");
		else builder.add("bits", attribute.size());
		builder.add("size", attribute.size());
		if (attribute.asData().isDateTime()) {
			builder.add("precision", "");//TODO
		} else if (attribute.asData().isDate()) {
			builder.add("precision", "");//TODO
		}
		return builder;
	}

	private Frame[] enums(Split realSplit, List<Split> leafs) {
		List<Frame> frames = new ArrayList<>();
		if (!leafs.contains(realSplit) && !realSplit.label().isEmpty())
			frames.add(new FrameBuilder("enum").add("value", realSplit.qn().replace(".", "-")).toFrame());
		for (Split leaf : leafs) {
			FrameBuilder builder = new FrameBuilder("enum").add("value", leaf.qn().replace(".", "-")).add("qn", leaf.qn());
			frames.add(builder.toFrame());
		}
		return frames.toArray(new Frame[0]);
	}

	private boolean isAligned(Data.Type attribute, int offset) {
		return (offset == 0 || log2(offset) % 1 == 0) && attribute.maxSize() == attribute.size();
	}

	private FrameBuilder process(WordBag wordBag, String name, int offset) {
		FrameBuilder builder = new FrameBuilder("attribute", "wordbag").
				add("name", name).
				add("type", wordBag.name$()).
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
		return data.isBool() || data.isInteger() || data.isLongInteger() || data.isId() || data.isReal();
	}

	private String[] words(WordBag wordBag) {
		return wordBag.asFromCode().wordList().stream().
				map(w -> w.name$() + "(" + w.value() + ")").toArray(String[]::new);
	}

	private Integer sizeOf(WordBag wordBag) {
		try {
			return !wordBag.isFromResource() ? (int) Math.ceil(log2(wordBag.asFromCode().wordList().size() + 1)) : (int) Math.ceil(log2(countLines(wordBag) + 1));
		} catch (IOException e) {
			return 0;
		}
	}

	private int countLines(WordBag wordBag) throws IOException {
		return (int) new BufferedReader(new InputStreamReader(wordBag.asFromResource().tsv().openStream())).lines().count();
	}

	private String transactionsPackage() {
		return rootPackage + ".transaction";
	}

	private Template template() {
		return Formatters.customize(new TransactionTemplate());
	}

	public static double log2(int N) {
		return (Math.log(N) / Math.log(2));
	}
}
