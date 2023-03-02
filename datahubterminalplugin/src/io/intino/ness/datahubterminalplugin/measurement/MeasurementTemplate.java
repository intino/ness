package io.intino.ness.datahubterminalplugin.measurement;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MeasurementTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\npublic class ")).output(mark("name", "firstUpperCase")).output(literal(" extends io.intino.alexandria.event.MeasurementEvent implements java.io.Serializable {\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("() {\n\t\t")).output(expression().output(literal("super(new io.intino.alexandria.event.MeasurementEvent(\"")).output(mark("parentSuper")).output(literal("\"))")).next(expression().output(literal("super(\"")).output(mark("name")).output(literal("\")")))).output(literal(";\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message) {\n\t\tsuper(message.get(\"ts\").getAsInstant(),message.get(\"sensor\").getAsString(),message.get(\"measurements\").as(String[].class),message.get(\"measurements\").as(double[].class));\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" ts(java.time.Instant ts) {\n\t\tsuper.ts(ts);\n\t\treturn this;\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" ss(String ss) {\n\t\tsuper.ss(ss);\n\t\treturn this;\n\t}\n\n\t")).output(expression().output(mark("value", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("value", "setter").multiple("\n\n"))).output(literal("\n\n\tpublic io.intino.alexandria.event.MessageEvent toMessageEvent() {\n\t\tio.intino.alexandria.message.Message message = new io.intino.alexandria.message.Message(this.getClass().getSimpleName());\n\t\tmessage.set(\"ts\", this.ts);\n\t\tmessage.set(\"ss\", this.sensor);\n\t\tmessage.set(\"measurements\", this.measurements);\n\t\tmessage.set(\"values\", this.values);\n\t\treturn new io.intino.alexandria.event.MessageEvent(message);\n\t}\n}")),
			rule().condition((trigger("getter"))).output(literal("public double ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn values[")).output(mark("index")).output(literal("];\n}")),
			rule().condition((trigger("setter"))).output(literal("public ")).output(mark("owner", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(double value) {\n\tvalues[")).output(mark("index")).output(literal("] = value;\n\treturn this;\n}"))
		);
	}
}