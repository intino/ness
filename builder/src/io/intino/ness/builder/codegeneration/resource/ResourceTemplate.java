package io.intino.ness.builder.codegeneration.resource;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ResourceTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\n")).output(mark("event")),
			rule().condition((trigger("event"))).output(literal("public class ")).output(mark("name", "firstUpperCase")).output(literal(" extends io.intino.alexandria.event.resource.ResourceEvent implements java.io.Serializable {\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.event.resource.ResourceEvent event) {\n\t\tsuper(\"")).output(mark("name")).output(literal("\", event.ss(), event.resource());\n\t\tts(event.ts());\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(String ss, io.intino.alexandria.Resource resource) {\n\t\tsuper(\"")).output(mark("name")).output(literal("\", ss, resource);\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(String ss, java.io.File file) {\n\t\tsuper(\"")).output(mark("name")).output(literal("\", ss, file);\n\t}\n\n\t@Override\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" ts(java.time.Instant ts) {\n\t\tsuper.ts(ts);\n\t\treturn this;\n\t}\n}"))
		);
	}
}