package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("entity","class"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\nimport io.intino.ness.master.model.*;\nimport io.intino.ness.master.reflection.EntityDefinition;\n\nimport java.time.*;\nimport java.util.*;\nimport java.util.stream.*;\n\nimport ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart;\n\npublic")).output(expression().output(literal(" ")).output(mark("isAbstract", "firstLowerCase"))).output(literal(" class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends ")).output(mark("parent")).output(literal(" {\n\n\tpublic static final EntityDefinition definition = new ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity.EntityDefinitionInternal(\"")).output(mark("name", "FirstUpperCase")).output(literal("\");\n\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\t")).output(mark("parent", "super")).output(literal("\n\t}\n\n\t@Override\n    public EntityDefinition getDefinition() {\n    \treturn definition;\n    }\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("expression", "expressionDeclaration").multiple("\n\n"))).output(literal("\n\n\t@Override\n\tprotected Collection<Attribute> initDeclaredAttributes() {\n\t\tCollection<Attribute> attributes = super.initDeclaredAttributes();\n\t\t")).output(expression().output(mark("attribute", "initAttribute").multiple("\n"))).output(literal("\n\t\treturn attributes;\n\t}\n}")),
			rule().condition((trigger("super"))).output(literal("super(id, datamart);")),
			rule().condition((trigger("abstract"))).output(literal("Abstract")),
			rule().condition((type("attribute")), not(type("inherited")), (trigger("initattribute"))).output(literal("attributes.add(new Attribute(\"")).output(mark("name")).output(literal("\", ")).output(mark("defaultValue")).output(literal("));")),
			rule().condition((type("word")), not(type("inherited")), (trigger("worddeclaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {")).output(mark("value").multiple(", ")).output(literal("}")),
			rule().condition((type("expression")), (trigger("expressiondeclaration"))).output(mark("modifier")).output(literal(" ")).output(mark("returnType")).output(literal(" ")).output(mark("name")).output(literal("(")).output(expression().output(mark("parameter", "parameterDeclaration").multiple(", "))).output(literal(") {\n\t")).output(mark("expr")).output(literal("\n}")),
			rule().condition((type("parameter")), (trigger("parameterdeclaration"))).output(mark("type")).output(literal(" ")).output(mark("name")),
			rule().condition((anyTypes("list","set")), (type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().<Collection<String>>as().stream()\n\t\t.map(datamart()::")).output(mark("typename", "firstLowerCase")).output(literal(")\n\t\t.collect(Collectors.to")).output(mark("collectionType")).output(literal("());\n}")),
			rule().condition((type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn datamart().")).output(mark("typename", "firstLowerCase")).output(literal("(attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().as(String.class));\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn attribute(\"")).output(mark("name", "firstLowerCase")).output(literal("\").value().<")).output(mark("type")).output(literal(">as();\n}"))
		);
	}
}