package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("entity", "class"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\nimport io.intino.ness.master.model.*;\nimport io.intino.ness.master.reflection.*;\n\nimport java.time.*;\nimport java.util.*;\nimport java.util.stream.*;\n\nimport ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart;\n\npublic")).output(expression().output(mark("isAbstract", "firstLowerCase"))).output(literal(" class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends ")).output(mark("parent")).output(literal(" {\n\n\tpublic static final EntityDefinition definition = new ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity.EntityDefinitionInternal(\"")).output(mark("name", "FirstUpperCase")).output(literal("\");\n\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\t")).output(mark("parent", "super")).output(literal("\n\t}\n\n\t@Override\n    public EntityDefinition getDefinition() {\n    \treturn definition;\n    }\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "translation").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("expression", "expressionDeclaration").multiple("\n\n"))).output(literal("\n\n\t@Override\n\tprotected Collection<Attribute> initDeclaredAttributes() {\n\t\tCollection<Attribute> attributes = super.initDeclaredAttributes();\n\t\t")).output(expression().output(mark("attribute", "initAttribute").multiple("\n"))).output(literal("\n\t\treturn attributes;\n\t}\n\t")).output(expression().output(mark("struct", "struct").multiple("\n\n"))).output(literal("\n}")),
				rule().condition((trigger("super"))).output(literal("super(id, datamart);")),
				rule().condition((trigger("abstract"))).output(literal("Abstract"))
		);
	}
}