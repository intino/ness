package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class AttributesTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("attribute")), not(type("inherited")), (trigger("initattribute"))).output(literal("attributes.add(new Attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\", null));")),
			rule().condition((type("word")), not(type("inherited")), (trigger("worddeclaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {")).output(mark("value").multiple(", ")).output(literal("}")),
			rule().condition((type("expression")), (trigger("expressiondeclaration"))).output(mark("modifier")).output(literal(" ")).output(mark("returnType")).output(literal(" ")).output(mark("name")).output(literal("(")).output(expression().output(mark("parameter", "parameterDeclaration").multiple(", "))).output(literal(") {\n\t")).output(mark("expr")).output(literal("\n}")),
			rule().condition((type("parameter")), (trigger("parameterdeclaration"))).output(mark("type")).output(literal(" ")).output(mark("name")),
			rule().condition((anyTypes("list","set")), (type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().<Collection<String>>as().stream()\n\t\t.map(datamart()::")).output(mark("typename", "firstLowerCase")).output(literal(")\n\t\t.collect(Collectors.to")).output(mark("collectionType")).output(literal("());\n}")),
			rule().condition((type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn datamart().")).output(mark("typename", "firstLowerCase")).output(literal("(attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().as(String.class));\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().<")).output(mark("type")).output(literal(">as();\n}")),
			rule().condition((trigger("equals"))).output(literal("Objects.equals(this.")).output(mark("name", "firstLowerCase")).output(literal("(), other.")).output(mark("name", "firstLowerCase")).output(literal("())")),
			rule().condition((trigger("tostring"))).output(literal("sb.append(this.")).output(mark("name", "firstLowerCase")).output(literal("()).append(',');")),
			rule().condition((type("attribute")), (trigger("get"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal("()")),
			rule().condition((type("attribute")), (trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((type("attribute")), (trigger("name"))).output(mark("name", "firstLowerCase")),
			rule().condition((trigger("putintomap"))).output(literal("put(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(");"))
		);
	}
}