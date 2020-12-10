package io.intino.ness.datahubterminalplugin.transaction;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TransactionTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\n")).output(expression().output(mark("dimensionsImport"))).output(literal("\n\n")).output(mark("transaction")),
			rule().condition((trigger("dimensionsimport"))).output(literal("import ")).output(mark("")).output(literal(".dimension.*;")),
			rule().condition((trigger("transaction"))).output(literal("public class ")).output(mark("name", "firstUpperCase")).output(literal(" extends io.intino.alexandria.led.Transaction {\n\t")).output(expression().output(mark("split"))).output(literal("\n\tpublic static final int SIZE = ")).output(mark("size")).output(literal(";\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("() {\n\t\tsuper(defaultByteStore());\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(io.intino.alexandria.led.buffers.store.ByteStore store) {\n\t\tsuper(store);\n    }\n\n\tpublic int size() {\n\t\treturn SIZE;\n\t}\n\n\tpublic java.util.Map<String, Object> values() {\n\t\tjava.util.Map<String, Object> values = new java.util.LinkedHashMap<>();\n\t\t")).output(expression().output(mark("attribute", "map").multiple("\n"))).output(literal("\n\t\treturn values;\n\t}\n\n\t")).output(expression().output(literal("protected long id() {")).output(literal("\n")).output(literal("\treturn ")).output(mark("id")).output(literal("();")).output(literal("\n")).output(literal("}"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("attribute", "setter").multiple("\n\n"))).output(literal("\n\n\tprivate static io.intino.alexandria.led.buffers.store.ByteStore defaultByteStore() {\n\t\tjava.nio.ByteBuffer buffer = io.intino.alexandria.led.util.memory.MemoryUtils.allocBuffer((long) SIZE);\n\t\tio.intino.alexandria.led.util.memory.MemoryAddress address = io.intino.alexandria.led.util.memory.MemoryAddress.of(buffer);\n\t\treturn new io.intino.alexandria.led.buffers.store.ByteBufferStore(buffer, address, 0, buffer.capacity());\n\t}\n}")),
			rule().condition((trigger("split"))).output(literal("public enum Split {\n\t")).output(mark("enum", "asEnum").multiple(", ")).output(literal(";\n\n\tpublic abstract String qn();\n\n\tpublic static Split splitByQn(String qn) {\n\t\treturn java.util.Arrays.stream(values()).filter(c -> c.qn().equals(qn)).findFirst().orElse(null);\n\t}\n}")),
			rule().condition((trigger("asenum"))).output(mark("value", "snakeCaseToCamelCase")).output(literal(" {\n\tpublic String qn() {\n\t\treturn \"")).output(mark("qn")).output(literal("\";\n\t}\n}")),
			rule().condition((trigger("nbits"))).output(literal("NBits")),
			rule().condition((trigger("map"))).output(literal("values.put(\"")).output(mark("name")).output(literal("\", ")).output(mark("name", "firstLowerCase")).output(literal("());")),
			rule().condition((allTypes("attribute","integer")), (trigger("getter"))).output(literal("public int ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn bitBuffer.get")).output(mark("aligned")).output(literal("Integer")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(");\n}")),
			rule().condition((allTypes("attribute","integer")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Integer")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","bool")), (trigger("getter"))).output(literal("public Boolean ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tbyte value = bitBuffer.getByteNBits(")).output(mark("offset")).output(literal(", 2);\n\treturn value == NULL ? null : value == 2;\n}")),
			rule().condition((allTypes("attribute","bool")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.setByteNBits(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", (byte) (")).output(mark("name", "firstLowerCase")).output(literal(" ? 2 : 1));\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","real")), (attribute("size", "32")), (trigger("getter"))).output(literal("public float ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn bitBuffer.getAlignedReal")).output(mark("size")).output(literal("Bits(")).output(mark("offset")).output(literal(");\n}")),
			rule().condition((allTypes("attribute","real")), (trigger("getter"))).output(literal("public double ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn bitBuffer.getAlignedReal")).output(mark("size")).output(literal("Bits(")).output(mark("offset")).output(literal(");\n}")),
			rule().condition((allTypes("attribute","real")), (attribute("size", "32")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(Float ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.setAlignedReal")).output(mark("size")).output(literal("Bits(")).output(mark("offset")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","real")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.setAlignedReal")).output(mark("size")).output(literal("Bits(")).output(mark("offset")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((type("attribute")), (anyTypes("id","longInteger")), (trigger("getter"))).output(literal("public long ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn bitBuffer.get")).output(mark("aligned")).output(literal("Long")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(");\n}")),
			rule().condition((type("attribute")), (anyTypes("id","longInteger")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Long")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","datetime")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn java.time.Instant.ofEpochMilli(bitBuffer.get")).output(mark("aligned")).output(literal("Long")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal("));\n}")),
			rule().condition((allTypes("attribute","datetime")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Long")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(".toEpochMilli());\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","date")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn java.time.LocalDate.ofEpochDay(Short.toUnsignedInt(bitBuffer.get")).output(mark("aligned")).output(literal("Short")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(")));\n}")),
			rule().condition((allTypes("attribute","date")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Short")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", (short) ")).output(mark("name", "firstLowerCase")).output(literal(".toEpochDay());\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","dimension","resource")), (trigger("getter"))).output(literal("public ")).output(mark("type", "firstUpperCase")).output(literal(".Category ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tfinal int value = bitBuffer.getIntegerNBits(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(");\n\treturn ")).output(mark("type", "firstUpperCase")).output(literal(".categoryByIndex(value);\n}")),
			rule().condition((allTypes("attribute","dimension","resource")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.setIntegerNBits(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? (int) NULL : ")).output(mark("type", "firstUpperCase")).output(literal(".categoryByName(")).output(mark("name", "firstLowerCase")).output(literal(").index);\n\treturn this;\n}\n\npublic ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "firstUpperCase")).output(literal(".Category ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.setIntegerNBits(")).output(mark("offset")).output(literal(", ")).output(mark("bits")).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? (int) NULL : ")).output(mark("name", "firstLowerCase")).output(literal(".index);\n\treturn this;\n}")),
			rule().condition((allTypes("attribute","dimension")), (trigger("getter"))).output(literal("public ")).output(mark("type", "firstUpperCase")).output(literal(".Category ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tfinal short value = bitBuffer.get")).output(mark("aligned")).output(literal("Short")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(");\n\treturn ")).output(mark("type", "firstUpperCase")).output(literal(".categoryByIndex(value);\n}")),
			rule().condition((allTypes("attribute","dimension")), (trigger("setter"))).output(literal("public ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Integer")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? (int) NULL : ")).output(mark("type", "firstUpperCase")).output(literal(".categoryByName(")).output(mark("name", "firstLowerCase")).output(literal(").index);\n\treturn this;\n}\n\npublic ")).output(mark("owner", "firstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type", "firstUpperCase")).output(literal(".Category ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tbitBuffer.set")).output(mark("aligned")).output(literal("Integer")).output(expression().output(mark("bits", "nbits"))).output(literal("(")).output(mark("offset")).output(expression().output(literal(", ")).output(mark("bits"))).output(literal(", ")).output(mark("name", "firstLowerCase")).output(literal(" == null ? (int) NULL : ")).output(mark("name", "firstLowerCase")).output(literal(".index);\n\treturn this;\n}"))
		);
	}
}