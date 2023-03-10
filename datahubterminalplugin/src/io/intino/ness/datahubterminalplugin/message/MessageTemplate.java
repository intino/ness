package io.intino.ness.datahubterminalplugin.message;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MessageTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\n")).output(mark("event")),
			rule().condition((trigger("event"))).output(literal("public class ")).output(mark("name", "firstUpperCase")).output(literal(" ")).output(expression().output(literal("extends ")).output(mark("parent"))).output(literal(" implements java.io.Serializable {\n\t")).output(expression().output(mark("attribute", "declaration").multiple("\n"))).output(literal("\n\t")).output(expression().output(mark("component", "declaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(String ss")).output(expression().output(literal(", String ")).output(mark("assertionId"))).output(literal(") {\n\t\tthis(new io.intino.alexandria.event.message.MessageEvent(\"")).output(mark("name")).output(literal("\", ss).toMessage()")).output(expression().output(literal(", ")).output(mark("assertionId"))).output(literal(");\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.event.message.MessageEvent event) {\n\t\tthis(event.toMessage());\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message) {\n\t\tthis(message")).output(expression().output(literal(", message.get(\"")).output(mark("assertionId")).output(literal("\").asString()"))).output(literal(");\n\t}\n\n\tprivate ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message, String id) {\n\t\tsuper(message);\n\t\tthis.message.set(\"id\", java.util.Objects.requireNonNull(id, \"Assertion Id cannot be null\"));\n\t}\n\n\t")).output(expression().output(literal("public String id() {\r")).output(literal("\n")).output(literal("\treturn message.get(\"")).output(mark("assertionId")).output(literal("\").asString();\r")).output(literal("\n")).output(literal("}"))).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" ts(java.time.Instant ts) {\n\t\tsuper.ts(ts);\n\t\treturn this;\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" ss(String ss) {\n\t\tsuper.ss(ss);\n\t\treturn this;\n\t}\n\n\tpublic static ")).output(mark("name", "firstUpperCase")).output(literal(" fromString(String event) {\n\t\treturn new ")).output(mark("name", "firstUpperCase")).output(literal("(new io.intino.alexandria.message.MessageReader(event).next());\n\t}\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("component", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "setter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("component", "setter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("component").multiple("\n\n"))).output(literal("\n\n\t@Override\n\tpublic io.intino.alexandria.message.Message toMessage() {\n\t\t")).output(expression().output(mark("attribute", "serializeTable").multiple("\n"))).output(literal("\n\t\treturn super.toMessage();\n\t}\n}")),
			rule().condition((trigger("put"))).output(literal("if (")).output(mark("")).output(literal(" == null) throw new IllegalArgumentException(\"Assertion Id cannot be null\");\nthis.message.set(\"id\", ")).output(mark("")).output(literal(");")),
			rule().condition((trigger("component"))).output(literal("public static class ")).output(mark("name", "firstUpperCase")).output(literal(" ")).output(expression().output(literal("extends ")).output(mark("parent"))).output(literal(" implements java.io.Serializable {\n\t")).output(expression().output(mark("attribute", "declaration").multiple("\n"))).output(literal("\n\t")).output(expression().output(mark("component", "declaration").multiple("\n"))).output(literal("\n\t")).output(expression().output(mark("parent", "semicolon")).next(expression().output(literal("protected io.intino.alexandria.message.Message message;")))).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("() {\n\t\t")).output(expression().output(literal("this(new io.intino.alexandria.message.Message(\"")).output(mark("parentSuper")).output(literal("\")")).next(expression().output(literal("this.message = new io.intino.alexandria.message.Message(\"")).output(mark("name")).output(literal("\");")))).output(literal("\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.message.Message message) {\n\t\t")).output(expression().output(literal("super(message)")).output(mark("parent", "semicolon")).next(expression().output(literal("this.message = message;")))).output(literal("\n\t}\n\n\t")).output(mark("attribute", "getter").multiple("\n\n")).output(literal("\n\n\t")).output(mark("component", "getter").multiple("\n\n")).output(literal("\n\n\t")).output(mark("attribute", "setter").multiple("\n\n")).output(literal("\n\n\t")).output(mark("component", "setter").multiple("\n\n")).output(literal("\n\n\t")).output(expression().output(mark("component").multiple("\n\n"))).output(literal("\n\n\tpublic io.intino.alexandria.message.Message toMessage() {\n\t\treturn this.message;\n\t}\n}")),
			rule().condition((type("default")), (trigger("super"))).output(literal("super(\")")),
			rule().condition((trigger("semicolon"))).output(literal(";\n")),
			rule().condition((allTypes("word","single")), (trigger("declaration"))).output(literal("public enum ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("words").multiple(", ")).output(literal("\n}\n")),
			rule().condition((allTypes("word","multiple")), (trigger("declaration"))).output(literal("public enum ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("words").multiple(", ")).output(literal("\n}\n")),
			rule().condition((type("table")), (trigger("declaration"))).output(literal("private ")).output(mark("package")).output(literal(".")).output(mark("table", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(" = null;")),
			rule().condition((type("component")), (trigger("declaration"))).output(literal("private java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("List = null;")),
			rule().condition((allTypes("word","single")), (trigger("getter"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "FirstLowerCase")).output(literal("() {\n\treturn !message.contains(\"")).output(mark("name", "FirstLowerCase")).output(literal("\") ? null : ")).output(mark("name", "FirstUpperCase")).output(literal(".valueOf(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").asString());\n}")),
			rule().condition((allTypes("word","multiple")), (trigger("getter"))).output(literal("public java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "FirstLowerCase")).output(literal("() {\n\tif (!message.contains(\"")).output(mark("name", "FirstLowerCase")).output(literal("\")) return java.util.Collections.emptyList();\n\treturn java.util.Arrays.stream(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(String[].class)).map(")).output(mark("name", "FirstUpperCase")).output(literal("::valueOf).collect(java.util.stream.Collectors.toUnmodifiableList());\n}")),
			rule().condition((type("table")), (trigger("getter"))).output(literal("public ")).output(mark("package")).output(literal(".")).output(mark("table", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn this.")).output(mark("name", "FirstLowerCase")).output(literal(" == null ? this.")).output(mark("name", "FirstLowerCase")).output(literal(" =  new ")).output(mark("package")).output(literal(".")).output(mark("table", "FirstUpperCase")).output(literal("(message.get(\"")).output(mark("name")).output(literal("\").asTable()) : this.")).output(mark("name", "FirstLowerCase")).output(literal(";\n}")),
			rule().condition((allTypes("primitive","single","date")), not(attribute("defaultvalue", "null")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("type")).output(literal(".ofInstant(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").asInstant(), java.time.ZoneId.systemDefault());\n}")),
			rule().condition((allTypes("primitive","single")), not(attribute("defaultvalue", "null")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as")).output(mark("simpleType")).output(literal("();\n}")),
			rule().condition((allTypes("primitive","single")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn !message.contains(\"")).output(mark("name", "FirstLowerCase")).output(literal("\") ? ")).output(mark("defaultValue")).output(literal(" : message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as")).output(mark("simpleType")).output(literal("();\n}")),
			rule().condition((allTypes("primitive","multiple")), (trigger("getter"))).output(literal("public java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn new java.util.ArrayList<")).output(mark("type", "FirstUpperCase")).output(literal(">(message.contains(\"")).output(mark("name", "FirstLowerCase")).output(literal("\") ? java.util.Arrays.asList(message.get(\"")).output(mark("name", "FirstLowerCase")).output(literal("\").as(")).output(mark("type")).output(literal("[].class)) : java.util.Collections.emptyList()) {\n\t\t@Override\n\t\tpublic boolean add(")).output(mark("type", "FirstUpperCase")).output(literal(" value) {\n\t\t\tsuper.add(value);\n\t\t\tmessage.append(\"")).output(mark("name", "FirstLowerCase")).output(literal("\", value);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic void add(int index, ")).output(mark("type", "FirstUpperCase")).output(literal(" element) {\n\t\t\tthrow new UnsupportedOperationException();\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean remove(Object value) {\n\t\t\tif (!(value instanceof ")).output(mark("type")).output(literal(")) return false;\n\t\t\tsuper.remove(value);\n\t\t\tmessage.remove(\"")).output(mark("name", "FirstLowerCase")).output(literal("\", value);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic ")).output(mark("type")).output(literal(" remove(int index) {\n\t\t\t")).output(mark("type")).output(literal(" type = get(index);\n\t\t\tremove(type);\n\t\t\treturn type;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean removeIf(java.util.function.Predicate<? super ")).output(mark("type", "FirstUpperCase")).output(literal("> filter) {\n\t\t\treturn removeAll(java.util.stream.IntStream.range(0, this.size()).filter(i -> filter.test(get(i))).mapToObj(this::get).collect(java.util.stream.Collectors.toList()));\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean removeAll(java.util.Collection<?> c) {\n\t\t\tc.forEach(this::remove);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean addAll(java.util.Collection<? extends ")).output(mark("type", "FirstUpperCase")).output(literal("> c) {\n\t\t\tc.forEach(this::add);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean addAll(int index, java.util.Collection<? extends ")).output(mark("type", "FirstUpperCase")).output(literal("> c) {\n\t\t\tthrow new UnsupportedOperationException();\n\t\t}\n\n\t\tpublic void clear() {\n\t\t\tsuper.clear();\n\t\t\tmessage.remove(\"")).output(mark("name", "FirstLowerCase")).output(literal("\");\n\t\t}\n\t};\n}")),
			rule().condition((allTypes("word","single")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tif (")).output(mark("name", "firstLowerCase")).output(literal(" == null) this.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\telse this.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".name());\n\treturn this;\n}")),
			rule().condition((allTypes("word","multiple")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\t")).output(mark("name", "firstLowerCase")).output(literal(".forEach(v -> this.message.append(\"")).output(mark("name", "firstLowerCase")).output(literal("\", v.name()));\n\treturn this;\n}")),
			rule().condition((allTypes("primitive","single")), (anyTypes("integer","double")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("primitive","single","date")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tif (")).output(mark("name", "firstLowerCase")).output(literal(" == null) this.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\telse this.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(".atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());\n\treturn this;\n}")),
			rule().condition((allTypes("primitive","single")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tif (")).output(mark("name", "firstLowerCase")).output(literal(" == null) this.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\telse this.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((type("table")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("package")).output(literal(".")).output(mark("table", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";\n\treturn this;\n}")),
			rule().condition((allTypes("primitive","multiple")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\t")).output(mark("name", "firstLowerCase")).output(literal(".forEach(v -> this.message.append(\"")).output(mark("name", "firstLowerCase")).output(literal("\", v));\n\treturn this;\n}")),
			rule().condition((allTypes("primitive","multiple")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\n\t")).output(mark("name", "firstLowerCase")).output(literal(".forEach(v -> this.message.append(\"")).output(mark("name", "firstLowerCase")).output(literal("\", v));\n\treturn this;\n}")),
			rule().condition((type("component")), not(type("single")), (trigger("getter"))).output(literal("public java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("List() {\n\tif (this.")).output(mark("name", "firstLowerCase")).output(literal("List != null) return this.")).output(mark("name", "firstLowerCase")).output(literal("List;\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal("List = new java.util.ArrayList<")).output(mark("type")).output(literal(">(message.components(\"")).output(mark("type")).output(literal("\").stream().map(c -> new ")).output(mark("type")).output(literal("(c)).collect(java.util.stream.Collectors.toList())) {\n\t\t@Override\n\t\tpublic boolean add(")).output(mark("type")).output(literal(" element) {\n\t\t\tsuper.add(element);\n\t\t\tmessage.add(element.toMessage());\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic void add(int index, ")).output(mark("type")).output(literal(" element) {\n\t\t\tthrow new UnsupportedOperationException();\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean remove(Object o) {\n\t\t\tif (!(o instanceof ")).output(mark("type")).output(literal(")) return false;\n\t\t\tsuper.remove(o);\n\t\t\tmessage.remove(((")).output(mark("type")).output(literal(") o).toMessage());\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic ")).output(mark("type")).output(literal(" remove(int index) {\n\t\t\t")).output(mark("type")).output(literal(" type = get(index);\n\t\t\tremove(type);\n\t\t\treturn type;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean removeIf(java.util.function.Predicate<? super ")).output(mark("type")).output(literal("> filter) {\n\t\t\treturn removeAll(java.util.stream.IntStream.range(0, this.size()).filter(i -> filter.test(get(i))).mapToObj(this::get).collect(java.util.stream.Collectors.toList()));\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean removeAll(java.util.Collection<?> c) {\n\t\t\tc.forEach(this::remove);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean addAll(java.util.Collection<? extends ")).output(mark("type")).output(literal("> c) {\n\t\t\tc.forEach(this::add);\n\t\t\treturn true;\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean addAll(int index, java.util.Collection<? extends ")).output(mark("type")).output(literal("> c) {\n\t\t\tthrow new UnsupportedOperationException();\n\t\t}\n\t};\n}")),
			rule().condition((type("component")), (type("single")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tjava.util.List<io.intino.alexandria.message.Message> components = message.components(\"")).output(mark("type")).output(literal("\");\n\treturn components.isEmpty() ? null : new ")).output(mark("type")).output(literal("(components.get(0));\n}")),
			rule().condition((type("component")), (type("single")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.message.components(\"")).output(mark("type")).output(literal("\").forEach(v -> this.message.remove(v));\n\tif (")).output(mark("name", "firstLowerCase")).output(literal(" != null) this.message.add(")).output(mark("name", "firstLowerCase")).output(literal(".toMessage());\n\treturn this;\n}")),
			rule().condition((allTypes("multiple","component")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("List(java.util.List<")).output(mark("type", "firstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tnew java.util.ArrayList(this.")).output(mark("name", "firstLowerCase")).output(literal("List()).forEach(v -> this.")).output(mark("name", "firstLowerCase")).output(literal("List.remove(v));\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal("List.addAll(")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((type("table")), (trigger("serialize"))).output(literal("if (this.")).output(mark("name", "firstLowerCase")).output(literal(" == null) this.message.remove(\"")).output(mark("name", "firstLowerCase")).output(literal("\");\nelse this.message.set(\"")).output(mark("name", "firstLowerCase")).output(literal("\", this.")).output(mark("name", "firstLowerCase")).output(literal(".serialize());"))
		);
	}
}