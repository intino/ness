package systems.intino.eventsourcing.datahubbuilder.codegeneration.resource;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.literal;
import static io.intino.itrules.template.outputs.Outputs.placeholder;

public class ResourceTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("root")).output(literal("package ")).output(placeholder("package", "ValidPackage")).output(literal(";\n\n")).output(placeholder("event")));
		rules.add(rule().condition(trigger("event")).output(literal("public class ")).output(placeholder("name", "firstUpperCase")).output(literal(" extends systems.intino.eventsourcing.event.resource.ResourceEvent implements java.io.Serializable {\n\n\tpublic ")).output(placeholder("name", "firstUpperCase")).output(literal("(systems.intino.eventsourcing.event.resource.ResourceEvent event) {\n\t\tsuper(\"")).output(placeholder("name")).output(literal("\", event.ss(), event.resource());\n\t\tts(event.ts());\n\t}\n\n\tpublic ")).output(placeholder("name", "firstUpperCase")).output(literal("(String ss, io.intino.alexandria.Resource resource) {\n\t\tsuper(\"")).output(placeholder("name")).output(literal("\", ss, resource);\n\t}\n\n\tpublic ")).output(placeholder("name", "firstUpperCase")).output(literal("(String ss, java.io.File file) {\n\t\tsuper(\"")).output(placeholder("name")).output(literal("\", ss, file);\n\t}\n\n\t@Override\n\tpublic ")).output(placeholder("name", "firstUpperCase")).output(literal(" ts(java.time.Instant ts) {\n\t\tsuper.ts(ts);\n\t\treturn this;\n\t}\n}")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}