package io.intino.ness.datahubterminalplugin.event;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TableTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("table"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\npublic class ")).output(mark("name", "firstUpperCase")).output(literal(" implements java.io.Serializable {\n\tprivate final java.util.List<Row> table;\n\tpublic enum Column {")).output(mark("column", "name").multiple(", ")).output(literal("}\n\n\t")).output(mark("column", "enum").multiple("\n\n")).output(literal("\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal("(java.util.List<io.intino.alexandria.message.Message.Value[]> table) {\n\t\tthis.table = table.stream().map(r -> new Row(r)).collect(java.util.stream.Collectors.toList());\n\t}\n\n\tpublic Row row(int index) {\n\t\treturn table.get(index);\n\t}\n\n\tpublic java.util.stream.Stream<Row> rows() {\n\t\treturn table.stream();\n\t}\n\n\tpublic ")).output(mark("name", "firstUpperCase")).output(literal(" add(Row row) {\n\t\tthis.table.add(row);\n\t\treturn this;\n\t}\n\n\t")).output(mark("column", "getter").multiple("\n\n")).output(literal("\n\n\tpublic String toString() {\n\t\treturn table.stream().\n\t\t\tmap(Object::toString).\n\t\t\tcollect(java.util.stream.Collectors.joining(\"\\n\"));\n\t}\n\n\tpublic String serialize() {\n\t\treturn table.stream().\n\t\t\tmap(Object::toString).\n\t\t\tcollect(java.util.stream.Collectors.joining(\"\u0001\"));\n\t}\n\n\tpublic static class Row {\n\t\tprivate io.intino.alexandria.message.Message.Value[] values;\n\n\t\tRow(io.intino.alexandria.message.Message.Value[] values) {\n\t\t\tthis.values = values;\n\t\t}\n\n\t\tpublic Row(")).output(mark("column", "signature").multiple(", ")).output(literal(") {\n\t\t\tvalues = new io.intino.alexandria.message.Message.Value[] {")).output(mark("column", "dataValue").multiple(", ")).output(literal("};\n\t\t}\n\n\t\tpublic Row(Object[] values) {\n\t\t\tvalues = java.util.Arrays.stream(values).map(v -> new io.intino.alexandria.message.DataValue(v)).toArray(io.intino.alexandria.message.Message.Value[]::new);\n\t\t}\n\n\t\tpublic String toString() {\n\t\t\treturn String.join(\"\\t\", java.util.Arrays.stream(values).map(io.intino.alexandria.message.Message.Value::asString).toArray(String[]::new));\n\t\t}\n\n\t\t")).output(mark("column", "rowGetter").multiple("\n\n")).output(literal("\n\n\t\t")).output(mark("column", "rowSetter").multiple("\n\n")).output(literal("\n\t}\n}")),
				rule().condition((type("column")), (trigger("name"))).output(mark("name")),
				rule().condition((type("column")), (trigger("datavalue"))).output(literal("new io.intino.alexandria.message.DataValue(")).output(mark("name")).output(literal(")")),
				rule().condition((allTypes("column", "word")), (trigger("enum"))).output(literal("public enum ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\t")).output(mark("word").multiple(", ")).output(literal(";\n}")),
				rule().condition((type("column")), (trigger("signature"))).output(mark("simpleType", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")),
				rule().condition((trigger("asenum"))).output(mark("value", "snakeCaseToCamelCase")).output(literal(" {\n\tpublic String qn() {\n\t\treturn \"")).output(mark("qn")).output(literal("\";\n\t}\n}")),
				rule().condition((allTypes("column", "word")), (trigger("getter"))).output(literal("public ")).output(mark("simpleType", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(int index) {\n\treturn ")).output(mark("name", "FirstUpperCase")).output(literal(".valueOf(table.get(Column.")).output(mark("name")).output(literal(".ordinal())[index].asString());\n}\n\npublic java.util.stream.Stream<")).output(mark("simpleType", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn java.util.List.of(table.get(Column.errorTag.ordinal())).stream().map(v -> ErrorTag.valueOf(v.asString()));\n}")),
				rule().condition((allTypes("column", "word")), (trigger("rowgetter"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "FirstUpperCase")).output(literal(".valueOf(values[(Column.")).output(mark("name")).output(literal(".ordinal())].asString());\n}")),
				rule().condition((type("column")), (trigger("rowgetter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn values[(Column.")).output(mark("name")).output(literal(".ordinal())].as")).output(mark("simpleType")).output(literal("();\n}")),
				rule().condition((type("column")), (trigger("getter"))).output(literal("public ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(int index) {\n\treturn table.get(index).")).output(mark("name")).output(literal("();\n}\n\npublic java.util.stream.Stream<")).output(mark("type")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\treturn table.stream().map(r -> r.")).output(mark("name")).output(literal("());\n}")),
				rule().condition((type("column")), (trigger("rowsetter"))).output(literal("public Row ")).output(mark("name", "firstLowerCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\tthis.values[")).output(mark("index")).output(literal("] = new io.intino.alexandria.message.DataValue(")).output(mark("name", "firstLowerCase")).output(literal(");\n\treturn this;\n}"))
		);
	}
}