package io.intino.builder.operations.codegeneration;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("entity","decorable"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("package")).output(literal(".MasterClient master) {\n\t\tsuper(id, master);\n\t}\n}")),
			rule().condition((allTypes("entity","class"))).output(literal("package ")).output(mark("package")).output(literal(".entities;\n\nimport io.intino.master.model.Triple;\n\nimport java.util.List;\nimport java.util.ArrayList;\nimport java.util.HashMap;\nimport java.util.Map;\nimport java.util.function.BiConsumer;\nimport java.util.Arrays;\nimport java.util.stream.Collectors;\n\npublic")).output(expression().output(literal(" ")).output(mark("isAbstract", "firstLowerCase"))).output(literal(" class ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal(" extends ")).output(mark("parent")).output(literal(" {\n\n\tprotected static final String LIST_SEP = \",\";\n\n\t")).output(expression().output(mark("attribute", "wordDeclaration").multiple("\n"))).output(literal("\n\n\tprotected final ")).output(mark("package")).output(literal(".MasterClient master;\n\n\t")).output(expression().output(mark("attribute", "field").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("abstract")).output(mark("name", "FirstUpperCase")).output(literal("(String id, ")).output(mark("package")).output(literal(".MasterClient master) {\n\t\t")).output(mark("parent", "super")).output(literal("\n\t\tthis.master = java.util.Objects.requireNonNull(master);\n\t}\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" add(Triple triple) {\n\t\tswitch(triple.predicate()) {\n\t\t\t")).output(expression().output(mark("attribute", "addSwitchCase").multiple("\n"))).output(literal("\n\t\t\tdefault: super.add(triple); break;\n\t\t}\n\t\treturn (")).output(mark("name", "FirstUpperCase")).output(literal(") this;\n\t}\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal(" remove(Triple triple) {\n\t\tswitch(triple.predicate()) {\n\t\t\t")).output(expression().output(mark("attribute", "removeSwitchCase").multiple("\n"))).output(literal("\n\t\t\tdefault: super.remove(triple); break;\n\t\t}\n\t\treturn (")).output(mark("name", "FirstUpperCase")).output(literal(") this;\n\t}\n\n\t")).output(expression().output(literal("public List<Triple> asTriples() {")).output(literal("\n")).output(literal("\tfinal java.util.ArrayList<Triple> triples = new java.util.ArrayList<>();")).output(literal("\n")).output(literal("\t")).output(mark("attribute", "asTriple").multiple("\n")).output(literal("\n")).output(literal("\tsuper.extraAttributes().entrySet().stream().map(e -> new Triple(id().get(), e.getKey(), e.getValue())).forEach(triples::add);")).output(literal("\n")).output(literal("\treturn triples;")).output(literal("\n")).output(literal("}"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "addAndRemoveMethod").multiple("\n\n"))).output(literal("\n}")),
			rule().condition((attribute("", "io.intino.master.model.Entity")), (trigger("super"))).output(literal("super(id);")),
			rule().condition((trigger("super"))).output(literal("super(id, master);")),
			rule().condition((trigger("addswitchcase"))).output(literal("case \"")).output(mark("name")).output(literal("\": add")).output(mark("name", "FirstUpperCase")).output(literal("(triple); break;")),
			rule().condition((trigger("removeswitchcase"))).output(literal("case \"")).output(mark("name")).output(literal("\": remove")).output(mark("name", "FirstUpperCase")).output(literal("(triple); break;")),
			rule().condition((trigger("addandremovemethod"))).output(literal("protected void add")).output(mark("name", "FirstUpperCase")).output(literal("(Triple triple) {\n\t")).output(mark("attribute", "add")).output(literal("\n}\n\nprotected void remove")).output(mark("name", "FirstUpperCase")).output(literal("(Triple triple) {\n\t")).output(mark("attribute", "remove")).output(literal("\n}")),
			rule().condition((type("boolean")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(Boolean::parseBoolean).collect(Collectors.toList());")),
			rule().condition((type("integer")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());")),
			rule().condition((type("double")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(Double::parseDouble).collect(Collectors.toList());")),
			rule().condition((type("long")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(Long::parseLong).collect(Collectors.toList());")),
			rule().condition((type("word")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(")).output(mark("type", "firstUpperCase")).output(literal("::valueOf).collect(Collectors.toList());")),
			rule().condition((type("string")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).collect(Collectors.toList());")),
			rule().condition((type("entity")), (type("list")), (type("component")), (trigger("add"))).output(literal("if(triple.value() == null) {\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = null;\n\treturn;\n}\n\nio.intino.master.serialization.MasterSerializer serializer = master.serializer();\nString[] items = triple.value().split(LIST_SEP);\nthis.")).output(mark("name", "firstLowerCase")).output(literal(" = new ArrayList<>(items.length);\n\nfor(String serializedItem : items) {\n\tMap<String, String> item = serializer.deserialize(serializedItem.trim());\n\t")).output(mark("entity", "FirstUpperCase")).output(literal(" entity = new ")).output(mark("entity", "FirstUpperCase")).output(literal("(item.get(\"id\"), master);\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(".add(entity);\n}")),
			rule().condition((type("entity")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).collect(Collectors.toList());")),
			rule().condition((type("date")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(s -> java.time.LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"))).collect(Collectors.toList()));")),
			rule().condition((type("datetime")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(s -> java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"))).collect(Collectors.toList()));")),
			rule().condition((type("instant")), (type("list")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Arrays.stream(triple.value().split(LIST_SEP)).map(String::trim).map(s -> java.time.Instant.ofEpochMilli(Long.parseLong(s))).collect(Collectors.toList()));\nthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : java.time.Instant.ofEpochMilli(Long.parseLong(triple.value()));")),
			rule().condition((type("boolean")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Boolean.parseBoolean(triple.value());")),
			rule().condition((type("integer")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Integer.parseInt(triple.value());")),
			rule().condition((type("double")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Double.parseDouble(triple.value());")),
			rule().condition((type("long")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : Long.parseLong(triple.value());")),
			rule().condition((type("word")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : ")).output(mark("type", "firstUpperCase")).output(literal(".valueOf(triple.value());")),
			rule().condition((type("string")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value();")),
			rule().condition((type("entity")), (type("component")), (trigger("add"))).output(literal("if(triple.value() == null) {\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = null;\n\treturn;\n}\n\nMap<String, String> attributes = master.serializer().deserialize(triple.value().trim());\nthis.")).output(mark("name", "firstLowerCase")).output(literal(" = new ")).output(mark("entity", "FirstUpperCase")).output(literal("(attributes.get(\"id\"), master);")),
			rule().condition((type("entity")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal("Reference = triple.value();")),
			rule().condition((type("date")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : java.time.LocalDate.parse(triple.value(), java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"));")),
			rule().condition((type("datetime")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : java.time.LocalDateTime.parse(triple.value(), java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"));")),
			rule().condition((type("instant")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : java.time.Instant.ofEpochMilli(Long.parseLong(triple.value()));")),
			rule().condition((type("map")), (trigger("add"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null\n\t? null\n\t: java.util.Arrays.stream(triple.value().split(\";\"))\n\t\t.map(e -> e.split(\"=\"))\n\t\t.collect(java.util.stream.Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));")),
			rule().condition((type("struct")), (trigger("add"))).output(literal("if (triple.value() == null) {\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = null;\n} else {\n\tList<String> values = java.util.Arrays.stream(triple.value().split(\",\", -1)).map(v -> v.trim()).collect(java.util.stream.Collectors.toList());\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = triple.value() == null ? null : ")).output(mark("struct", "call")).output(literal(";\n}")),
			rule().condition((type("list")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = null;")),
			rule().condition((type("boolean")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = false;")),
			rule().condition((type("integer")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = 0;")),
			rule().condition((type("double")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = 0;")),
			rule().condition((type("long")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = 0;")),
			rule().condition((type("entity")), (type("component")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = null;")),
			rule().condition((type("entity")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal("Reference = null;")),
			rule().condition((trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = null;")),
			rule().condition((type("struct")), (trigger("remove"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = null;")),
			rule().condition((type("word")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".name()));")),
			rule().condition((type("entity")), (type("component")), (type("list")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", master.serializer().serialize(")).output(mark("name", "firstLowerCase")).output(literal(".stream().flatMap(e -> e.asTriples().stream()).collect(Collectors.toMap(Triple::predicate, Triple::value)))));")),
			rule().condition((type("entity")), (type("component")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal(")));")),
			rule().condition((type("entity")), (type("list")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.join(LIST_SEP, ")).output(mark("name", "firstLowerCase")).output(literal(")));")),
			rule().condition((type("entity")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal("Reference)));")),
			rule().condition((anyTypes("date","datetime")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".format(java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\"))));")),
			rule().condition((type("instant")), (trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal(".toEpochMilli())));")),
			rule().condition((trigger("astriple"))).output(literal("triples.add(new Triple(id().get(), \"")).output(mark("name", "firstLowerCase")).output(literal("\", String.valueOf(")).output(mark("name", "firstLowerCase")).output(literal(")));")),
			rule().condition((type("word")), (trigger("worddeclaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {")).output(mark("value").multiple(", ")).output(literal("}")),
			rule().condition((type("struct")), (trigger("field"))).output(literal("protected ")).output(mark("package")).output(literal(".structs.")).output(mark("struct", "structName")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(expression().output(literal(" = ")).output(mark("defaultValue"))).output(literal(";")),
			rule().condition((type("entity")), (type("component")), (type("list")), (trigger("field"))).output(literal("protected List<")).output(mark("entity", "firstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("entity")), (type("component")), (trigger("field"))).output(literal("protected ")).output(mark("entity", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("entity")), (type("list")), (trigger("field"))).output(literal("protected List<String> ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("entity")), (trigger("field"))).output(literal("protected String ")).output(mark("name", "firstLowerCase")).output(literal("Reference;")),
			rule().condition((type("date")), (trigger("field"))).output(literal("protected java.time.LocalDate ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("datetime")), (trigger("field"))).output(literal("protected java.time.LocalDateTime ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("instant")), (trigger("field"))).output(literal("protected java.time.Instant ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition(not(type("entity")), (trigger("field"))).output(literal("protected ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(expression().output(literal(" = ")).output(mark("defaultValue"))).output(literal(";")),
			rule().condition((type("word")), (trigger("defaultvalue"))).output(mark("type")).output(literal(".")).output(mark("value")),
			rule().condition((trigger("defaultvalue"))).output(mark("value")),
			rule().condition((type("struct")), (trigger("getter"))).output(literal("public ")).output(mark("package")).output(literal(".structs.")).output(mark("struct", "structName")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("entity")), (type("component")), (type("list")), (trigger("getter"))).output(literal("public List<")).output(mark("entity", "firstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("entity")), (type("component")), (trigger("getter"))).output(literal("public ")).output(mark("entity", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("entity")), (type("list")), (trigger("getter"))).output(literal("public List<")).output(mark("entity", "firstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(".stream().map(master::")).output(mark("entity", "firstLowerCase")).output(literal(").collect(Collectors.toList());\n}")),
			rule().condition((type("entity")), (trigger("getter"))).output(literal("public ")).output(mark("entity", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn master.")).output(mark("entity", "firstLowerCase")).output(literal("(")).output(mark("name", "firstLowerCase")).output(literal("Reference);\n}")),
			rule().condition((type("date")), (trigger("getter"))).output(literal("public java.time.LocalDate ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("datetime")), (trigger("getter"))).output(literal("public java.time.LocalDateTime ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((type("instant")), (trigger("getter"))).output(literal("public java.time.Instant ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal(";\n}")),
			rule().condition((trigger("abstract"))).output(literal("Abstract")),
			rule().condition((trigger("structname"))).output(mark("name", "firstUpperCase")),
			rule().condition((trigger("call"))).output(literal("new ")).output(mark("package")).output(literal(".structs.")).output(mark("name", "firstUpperCase")).output(literal("(")).output(mark("attribute", "parse").multiple(", ")).output(literal(")")),
			rule().condition((type("boolean")), (trigger("parse"))).output(literal("Boolean.parseBoolean(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("int")), (trigger("parse"))).output(literal("Integer.parseInt(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("double")), (trigger("parse"))).output(literal("Double.parseDouble(values.get(")).output(mark("index")).output(literal("))")),
			rule().condition((type("date")), (trigger("parse"))).output(literal("java.time.LocalDate.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((type("datetime")), (trigger("parse"))).output(literal("java.time.LocalDateTime.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((type("instant")), (trigger("parse"))).output(literal("java.time.Instant.parse(values.get(")).output(mark("index")).output(literal("));")),
			rule().condition((trigger("parse"))).output(literal("values.get(")).output(mark("index")).output(literal(")"))
		);
	}
}