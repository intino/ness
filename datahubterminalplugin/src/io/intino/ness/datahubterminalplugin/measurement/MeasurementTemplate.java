package io.intino.ness.datahubterminalplugin.measurement;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MeasurementTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("measurement"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\npublic class ")).output(mark("name", "firstUpperCase")).output(literal(" extends io.intino.alexandria.event.measurement.MeasurementEvent implements java.io.Serializable {\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(String sensor) {\n\t\tsuper(\"")).output(mark("name", "firstUpperCase")).output(literal("\", sensor, java.time.Instant.now(), new String[]{")).output(mark("value", "name").multiple(",")).output(literal("}, new double[")).output(mark("size")).output(literal("]);\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(String sensor, java.time.Instant ts) {\n\t\tsuper(\"")).output(mark("name", "firstUpperCase")).output(literal("\", sensor, ts, new String[]{")).output(mark("value", "name").multiple(",")).output(literal("}, new double[")).output(mark("size")).output(literal("]);\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message) {\n\t\tsuper(\"")).output(mark("name", "firstUpperCase")).output(literal("\", message.get(\"sensor\").asString(), message.get(\"ts\").asInstant(), message.get(\"measurements\").as(String[].class), message.get(\"measurements\").as(double[].class));\n\t}\n\n\t")).output(expression().output(mark("value", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("value", "setter").multiple("\n\n"))).output(literal("\n\n\tpublic io.intino.alexandria.event.message.MessageEvent toMessageEvent() {\n\t\tio.intino.alexandria.message.Message message = new io.intino.alexandria.message.Message(this.getClass().getSimpleName());\n\t\tmessage.set(\"ts\", this.ts);\n\t\tmessage.set(\"ss\", this.source);\n\t\tjava.util.Arrays.stream(measurements).forEach(m -> message.append(\"measurements\", m));\n\t\tjava.util.Arrays.stream(values).forEach(m -> message.append(\"values\", m));\n\t\treturn new io.intino.alexandria.event.message.MessageEvent(message);\n\t}\n}")),
			rule().condition((trigger("getter"))).output(literal("public double ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn values[")).output(mark("index")).output(literal("];\n}")),
			rule().condition((trigger("name"))).output(mark("name")),
			rule().condition((trigger("setter"))).output(literal("public ")).output(mark("owner", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(double value) {\n\tvalues[")).output(mark("index")).output(literal("] = value;\n\treturn this;\n}"))
		);
	}
}