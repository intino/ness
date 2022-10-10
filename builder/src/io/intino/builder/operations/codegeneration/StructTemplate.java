package io.intino.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class StructTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("struct"))).output(literal("package ")).output(mark("package")).output(literal(".structs;\n\nimport java.util.Objects;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n\t")).output(mark("attribute", "field").multiple("\n")).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(")).output(mark("attribute", "parameter").multiple(", ")).output(literal(") {\n\t\t")).output(mark("attribute", "assign").multiple("\n")).output(literal("\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n\n")).output(literal("\n\n\t@Override\n\tpublic boolean equals(Object obj) {\n\t\tif(obj == null) return false;\n\t\tif(!obj.getClass().equals(getClass())) return false;\n\t\t")).output(mark("name", "FirstUpperCase")).output(literal(" other = (")).output(mark("name", "FirstUpperCase")).output(literal(") obj;\n\t\treturn ")).output(mark("attribute", "equals").multiple(" && ")).output(literal(";\n\t}\n\n\t@Override\n    public int hashCode() {\n    \treturn Objects.hash(")).output(mark("attribute").multiple(", ")).output(literal(");\n    }\n\n\t@Override\n\tpublic String toString() {\n\t\tStringBuilder sb = new StringBuilder(\"")).output(mark("name", "FirstUpperCase")).output(literal(" {\");\n\t\t")).output(mark("attribute", "toString").multiple("\n")).output(literal("\n\t\tsb.delete(sb.length() - \", \".length(), sb.length());\n\t\treturn sb.append(\"}\").toString();\n\t}\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("assign"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((trigger("field"))).output(literal("private final ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((trigger("equals"))).output(literal("Objects.equals(this.")).output(mark("name", "firstLowerCase")).output(literal(", other.")).output(mark("name", "firstLowerCase")).output(literal(")")),
			rule().condition((trigger("tostring"))).output(literal("sb.append(\"")).output(mark("name", "firstLowerCase")).output(literal("=\").append(this.")).output(mark("name", "firstLowerCase")).output(literal(").append(\", \");")),
			rule().condition((type("attribute"))).output(literal("this.")).output(mark("name", "firstLowerCase"))
		);
	}
}