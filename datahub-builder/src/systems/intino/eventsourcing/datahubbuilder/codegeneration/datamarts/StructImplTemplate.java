package systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.*;

public class StructImplTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("struct", "class")).output(literal("\n")).output(expression().output(placeholder("standalone", "header"))).output(literal("\n\npublic")).output(expression().output(placeholder("static"))).output(literal(" class ")).output(placeholder("name", "FirstUpperCase")).output(literal(" extends ")).output(placeholder("parent")).output(literal(" {\n\t")).output(expression().output(placeholder("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(placeholder("name", "FirstUpperCase")).output(literal("(")).output(placeholder("datamart", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\tsuper(datamart);\n\t}\n\n\t")).output(expression().output(placeholder("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(placeholder("expression", "expressionDeclaration").multiple("\n\n"))).output(literal("\n\n\t@Override\n\tpublic StructDefinition getDefinition() {\n\t\treturn definition;\n\t}\n\n\t@Override\n\tpublic boolean equals(Object obj) {\n\t\tif(obj == null) return false;\n\t\tif(!obj.getClass().equals(getClass())) return false;\n\t\t")).output(placeholder("name", "FirstUpperCase")).output(literal(" other = (")).output(placeholder("name", "FirstUpperCase")).output(literal(") obj;\n\t\treturn ")).output(placeholder("attribute", "equals").multiple(" && ")).output(literal(";\n\t}\n\n\t@Override\n    public int hashCode() {\n    \treturn Objects.hash(")).output(placeholder("attribute", "get").multiple(", ")).output(literal(");\n    }\n\n\t@Override\n\tpublic String toString() {\n\t\tStringBuilder sb = new StringBuilder();\n\t\t")).output(placeholder("attribute", "toString").multiple("\n")).output(literal("\n\t\tif(sb.length() > 0) sb.setLength(sb.length() - 1);\n\t\treturn sb.toString();\n\t}\n\n\t@Override\n\tprotected Collection<Attribute> initDeclaredAttributes() {\n\t\tCollection<Attribute> attributes = super.initDeclaredAttributes();\n\t\t")).output(expression().output(placeholder("attribute", "initAttribute").multiple("\n"))).output(literal("\n\t\treturn attributes;\n\t}\n\n\t")).output(expression().output(placeholder("struct", "struct").multiple("\n\n"))).output(literal("\n}")));
		rules.add(rule().condition(trigger("header")).output(literal("package ")).output(placeholder("package")).output(literal(".structs;\n\nimport io.intino.ness.master.reflection.StructDefinition;\n\nimport java.time.*;\nimport java.util.*;\nimport java.util.stream.*;\n\nimport ")).output(placeholder("package")).output(literal(".")).output(placeholder("datamart", "FirstUpperCase")).output(literal("Datamart;")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}