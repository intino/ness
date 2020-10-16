package io.intino.ness.datahubterminalplugin.schema;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class SchemaTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\n")).output(mark("schema")),
			rule().condition((trigger("schema"))).output(literal("public class ")).output(mark("name", "firstUpperCase")).output(literal(" extends io.intino.alexandria.led.Schema {\n\t")).output(expression().output(mark("split"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "declaration").multiple("\n\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("() {\n\t\tsuper(defaultByteStore());\n\t}\n\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.led.buffers.store.ByteStore store) {\n\t\tsuper(store);\n    }\n\n\tpublic int size() {\n\t\treturn ")).output(mark("size")).output(literal(";\n\t}\n\n\t@Override\n\tpublic long id() {\n\t\treturn getAlignedLong(0);\n\t}\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "setter").multiple("\n\n"))).output(literal("\n\n\tprivate static io.intino.alexandria.led.buffers.store.ByteStore defaultByteStore() {\n\t\tjava.nio.ByteBuffer buffer = io.intino.alexandria.led.util.MemoryUtils.allocBuffer(")).output(mark("size")).output(literal(");\n\t\tio.intino.alexandria.led.util.MemoryAddress address = io.intino.alexandria.led.util.MemoryAddress.of(buffer);\n\t\treturn new io.intino.alexandria.led.buffers.store.ByteBufferStore(buffer, address, 0, buffer.capacity());\n\t}\n}")),
			rule().condition((trigger("split"))).output(literal("public enum Split {\n\t")).output(mark("split", "qn").multiple(", ")).output(literal(";\n\n\tpublic abstract String qn();\n\n\tpublic static Split splitByQn(String qn) {\n\t\treturn java.util.Arrays.stream(values()).filter(c -> c.qn().equals(qn)).findFirst().orElse(null);\n\t}\n}")),
			rule().condition((trigger("qn"))).output(mark("value", "snakeCaseToCamelCase")).output(literal(" {\n\tpublic String qn() {\n\t\treturn \"")).output(mark("qn")).output(literal("\";\n\t}\n}")),
			rule().condition((allTypes("attribute","integer")), (trigger("getter"))).output(literal("public Integer ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn (int) getInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(");\n}")),
			rule().condition((allTypes("attribute","integer")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","double")), (trigger("getter"))).output(literal("public Integer ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn getAlignedDouble(io.intino.alexandria.led.util.BitUtils.byteIndex(")).output(mark("offset")).output(literal("));\n}")),
			rule().condition((allTypes("attribute","double")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetAlignedDouble(io.intino.alexandria.led.util.BitUtils.byteIndex(")).output(mark("offset")).output(literal("), ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","long")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn getAlignedLong(")).output(mark("offset")).output(literal(" / Byte.SIZE);\n}")),
			rule().condition((allTypes("attribute","long")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetAlignedLong(")).output(mark("offset")).output(literal(" / Byte.SIZE, ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","datetime")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn java.time.Instant.ofEpochMilli(getAlignedLong(")).output(mark("offset")).output(literal(" / Byte.SIZE));\n}")),
			rule().condition((allTypes("attribute","datetime")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetAlignedLong(")).output(mark("offset")).output(literal(" / Byte.SIZE, ")).output(mark("name", "firstLowerCase")).output(literal(".toEpochMilli());\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","wordbag","resource")), (trigger("getter"))).output(literal("public ")).output(mark("type", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tfinal int value = (int) getInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(");\n\treturn value == NULL ? null : ")).output(mark("name", "firstUpperCase")).output(literal(".values().get(value);\n}")),
			rule().condition((allTypes("attribute","wordbag","resource")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? NULL : ")).output(mark("name", "firstLowerCase")).output(literal(".indexOf(")).output(mark("name", "firstLowerCase")).output(literal("));\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","wordbag")), (trigger("getter"))).output(literal("public ")).output(mark("type", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tfinal int value = (int) getInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(");\n\treturn value == NULL ? null : ")).output(mark("name", "firstUpperCase")).output(literal(".nameOf(value);\n}")),
			rule().condition((allTypes("attribute","wordbag")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tsetInteger(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? NULL : ")).output(mark("name", "firstLowerCase")).output(literal(".value());\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","wordbag","resource")), (trigger("declaration"))).output(literal("public static class ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\tprivate static final java.util.Map<Integer, String> values;\n\n\tstatic {\n\t\tvalues = new java.io.BufferedReader(new java.io.InputStreamReader(")).output(mark("name", "firstUpperCase")).output(literal(".class.getResourceAsStream(\"")).output(mark("resource")).output(literal("\"))).\n\t\t\tlines().map(l -> l.split(\"\\t\")).\n\t\t\tcollect(java.util.stream.Collectors.toMap(l -> Integer.parseInt(l[0]), l -> l[1]));\n\t}\n\n\tpublic static java.util.Map<Integer, String> values() {\n\t\treturn values;\n\t}\n\n\tpublic static long indexOf(String i) {\n\t\tjava.util.Map.Entry<Integer, String> e = values.entrySet().stream().filter(en -> en.getValue().equals(i)).findFirst().orElse(null);\n\t\treturn e == null ? NULL : e.getKey();\n\t}\n}")),
			rule().condition((allTypes("attribute","wordbag")), (trigger("declaration"))).output(literal("public enum ")).output(mark("name", "firstUpperCase")).output(literal(" {\n\t")).output(mark("word").multiple(", ")).output(literal(";\n\tint value;\n\n\t")).output(mark("name", "firstUpperCase")).output(literal("(int value) {\n\t\tthis.value = value;\n\t}\n\n\tpublic int value() {\n\t\treturn value;\n\t}\n\n\tpublic static ")).output(mark("type", "firstUpperCase")).output(literal(" nameOf(int value) {\n\t\treturn java.util.Arrays.stream(values()).filter(v -> v.value() == value).findFirst().orElse(null);\n\t}\n}"))
		);
	}
}