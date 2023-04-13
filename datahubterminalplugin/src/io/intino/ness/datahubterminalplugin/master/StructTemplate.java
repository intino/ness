package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class StructTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("struct","class"))).output(literal("\n")).output(expression().output(mark("standalone", "header"))).output(literal("\n\npublic")).output(expression().output(mark("static"))).output(literal(" class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends ")).output(mark("parent")).output(literal(" {\n\n\tpublic static final StructDefinition definition = new ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Struct.StructDefinitionInternal(\"")).output(mark("definitionname", "FirstUpperCase")).output(literal("\");\n\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\tsuper(datamart, java.util.Collections.emptyMap());\n\t}\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart, ")).output(mark("attribute", "parameter").multiple(", ")).output(literal(") {\n\t\tsuper(datamart, asMap(")).output(expression().output(mark("attribute", "name").multiple(", "))).output(literal("));\n\t}\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("expression", "expressionDeclaration").multiple("\n\n"))).output(literal("\n\n\t@Override\n\tpublic StructDefinition getDefinition() {\n\t\treturn definition;\n\t}\n\n\t@Override\n\tpublic boolean equals(Object obj) {\n\t\tif(obj == null) return false;\n\t\tif(!obj.getClass().equals(getClass())) return false;\n\t\t")).output(mark("name", "FirstUpperCase")).output(literal(" other = (")).output(mark("name", "FirstUpperCase")).output(literal(") obj;\n\t\treturn ")).output(mark("attribute", "equals").multiple(" && ")).output(literal(";\n\t}\n\n\t@Override\n    public int hashCode() {\n    \treturn Objects.hash(")).output(mark("attribute", "get").multiple(", ")).output(literal(");\n    }\n\n\t@Override\n\tpublic String toString() {\n\t\tStringBuilder sb = new StringBuilder();\n\t\t")).output(mark("attribute", "toString").multiple("\n")).output(literal("\n\t\tif(sb.length() > 0) sb.setLength(sb.length() - 1);\n\t\treturn sb.toString();\n\t}\n\n\tprivate static java.util.Map<String, Object> asMap(")).output(mark("attribute", "parameter").multiple(", ")).output(literal(") {\n\t\treturn new java.util.LinkedHashMap<>() {{\n\t\t\t")).output(expression().output(mark("attribute", "putIntoMap").multiple("\n"))).output(literal("\n\t\t}};\n\t}\n\t")).output(expression().output(mark("struct", "struct").multiple("\n\n"))).output(literal("\n}")),
			rule().condition((trigger("header"))).output(literal("package ")).output(mark("package")).output(literal(".structs;\n\nimport io.intino.ness.master.reflection.StructDefinition;\n\nimport java.time.*;\nimport java.util.*;\nimport java.util.stream.*;\n\nimport ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart;"))
		);
	}
}