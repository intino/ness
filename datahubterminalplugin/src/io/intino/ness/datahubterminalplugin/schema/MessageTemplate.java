package io.intino.ness.datahubterminalplugin.schema;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MessageTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\n")).output(mark("schema")),
				rule().condition((trigger("schema"))).output(literal("public ")).output(expression().output(mark("inner"))).output(literal(" class ")).output(mark("name", "firstUpperCase")).output(literal(" ")).output(expression().output(literal("extends ")).output(mark("parent"))).output(literal(" implements java.io.Serializable {\n\t")).output(expression().output(mark("parent", "delegate")).next(expression().output(literal("io.intino.alexandria.message.Message message;")))).output(literal("\n\t")).output(mark("attribute", "declaration").multiple("\n")).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message) {\n\t\t")).output(expression().output(mark("parent", "super")).next(expression().output(literal("this.message = message;")))).output(literal("\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n\n")).output(literal("\n\n\t")).output(mark("attribute", "setter").multiple("\n\n")).output(literal("\n\n\n\tpublic io.intino.alexandria.message.Message get() {\n\t\treturn this.message;\n\t}\n\n\t")).output(expression().output(mark("schema").multiple("\n\n"))).output(literal("\n}")),
				rule().condition((trigger("super"))).output(literal("super(message);")),
				rule().condition((trigger("delegate"))).output(literal(";\n")),
				rule().condition((allTypes("word", "single")), (trigger("declaration"))).output(literal("public enum ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("words").multiple(", ")).output(literal("\n}\n")),
				rule().condition((allTypes("word", "multiple")), (trigger("declaration"))).output(literal("public enum ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("words").multiple(", ")).output(literal("\n}\n")),
				rule().condition((allTypes("word", "single")), (trigger("getter"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "FirstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "FirstUpperCase")).output(literal(".valueOf(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(String.class));\n}")),
				rule().condition((allTypes("word", "multiple")), (trigger("getter"))).output(literal("public java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "FirstLowerCase")).output(literal("() {\n\treturn java.util.Arrays.asList(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(String[].class)).map(v -> ")).output(mark("name", "FirstUpperCase")).output(literal(".valueOf(v)).collect(java.util.Collectors.toList());\n}")),
				rule().condition((allTypes("primitive", "single")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(")).output(mark("type")).output(literal(".class);\n}")),
				rule().condition((allTypes("primitive", "multiple")), (trigger("getter"))).output(literal("public java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn java.util.Arrays.asList(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(")).output(mark("type")).output(literal("[].class));\n}")),
				rule().condition((allTypes("word", "single")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".toString());\n\treturn this;\n}")),
				rule().condition((allTypes("word", "multiple")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\t//TODO inl\n\treturn this;\n}")),
				rule().condition((allTypes("primitive", "single")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
				rule().condition((allTypes("primitive", "multiple")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\t")).output(mark("name", "firstLowerCase")).output(literal(".forEach(v -> this.message.append(\"")).output(mark("name", "firstLowerCase")).output(literal("\", v));\n\treturn this;\n}")),
				rule().condition(not(type("primitive")), (type("single")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tjava.util.List<io.intino.alexandria.message.Message> components = message.components(\"")).output(mark("name")).output(literal("\");\n\treturn components.isEmpty() ? null : new ")).output(mark("type")).output(literal("(components.get(0));\n}")),
				rule().condition(not(type("primitive")), (type("single")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.components(\"")).output(mark("type")).output(literal("\").forEach(v -> this.message.remove(v));\n\tthis.message.add(")).output(mark("name", "firstLowerCase")).output(literal(".get());\n\treturn this;\n}")),
				rule().condition((allTypes("multiple", "member")), (trigger("setter"))).output(literal("public ")).output(mark("element", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type", "firstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.components(\"")).output(mark("type")).output(literal("\").forEach(v -> this.message.remove(v));\n\t")).output(mark("name", "firstLowerCase")).output(literal(".forEach(v -> this.message.add(v.get()));\n\treturn this;\n}"))
		);
	}
}