package io.intino.alexandria.columnar.exporters;

import org.siani.itrules.LineSeparator;
import org.siani.itrules.Template;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.LF;

public class ArffTemplate extends Template {

	protected ArffTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new ArffTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
				rule().add((condition("type", "arff"))).add(literal("@RELATION relation\n\n")).add(mark("attribute").multiple("\n")).add(literal("\n\n@DATA\n")),
				rule().add((condition("trigger", "attribute"))).add(literal("@ATTRIBUTE ")).add(mark("name")).add(literal(" ")).add(mark("type")),
				rule().add((condition("type", "Nominal")), (condition("trigger", "type"))).add(literal("{")).add(mark("value", "quoted").multiple(",")).add(literal("}")),
				rule().add((condition("type", "Date")), (condition("trigger", "type"))).add(literal("DATE \"")).add(mark("format")).add(literal("\"")),
				rule().add((condition("type", "Numeric")), (condition("trigger", "type"))).add(literal("NUMERIC")),
				rule().add((condition("type", "String")), (condition("trigger", "type"))).add(literal("string")),
				rule().add((condition("trigger", "quoted"))).add(literal("\"")).add(mark("value")).add(literal("\""))
		);
		return this;
	}
}