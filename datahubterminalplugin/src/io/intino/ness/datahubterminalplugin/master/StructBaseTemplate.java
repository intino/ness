package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class StructBaseTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("struct","base"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport io.intino.ness.master.model.Concept;\nimport io.intino.ness.master.model.Struct;\nimport io.intino.ness.master.reflection.AttributeDefinition;\nimport io.intino.ness.master.reflection.StructDefinition;\n\nimport java.util.ArrayList;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.Objects;\n\npublic abstract class ")).output(mark("datamart", "FirstUpperCase")).output(literal("Struct implements Struct {\n\n\tprivate final ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart;\n\tprivate final Map<String, Attribute> attributes;\n\n\tpublic ")).output(mark("datamart", "FirstUpperCase")).output(literal("Struct(")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart, Map<String, Object> attributes) {\n\t\tthis.datamart = datamart;\n\t\tthis.attributes = new java.util.LinkedHashMap<>(attributes.size());\n\t\tattributes.forEach((k, v) -> this.attributes.put(k, new Attribute(k, v)));\n\t}\n\n\t@Override\n\tpublic Attribute attribute(String name) {\n\t\treturn attributes.get(name);\n\t}\n\n\t@Override\n\tpublic List<Concept.Attribute> attributes() {\n\t\treturn new ArrayList<>(attributes.values());\n\t}\n\n\t@Override\n\tpublic ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart() {\n\t\treturn datamart;\n\t}\n\n\t@Override\n\tpublic void addChangeListener(ChangeListener listener) {\n\t\t// Structs are read only\n\t}\n\n\t@Override\n\tpublic boolean equals(Object o) {\n\t\tif (this == o) return true;\n\t\tif (o == null || getClass() != o.getClass()) return false;\n\t\treturn attributes.equals(((")).output(mark("datamart", "FirstUpperCase")).output(literal("Struct)o).attributes);\n\t}\n\n\t@Override\n\tpublic int hashCode() {\n\t\treturn attributes.hashCode();\n\t}\n\n\t@Override\n\tpublic String toString() {\n\t\treturn getClass().getSimpleName() + \": \" + attributes;\n\t}\n\n\tprotected final class Attribute implements Concept.Attribute {\n\n\t\tprivate final String name;\n\t\tprivate final Value value;\n\n\t\tpublic Attribute(String name, Object initialValue) {\n\t\t\tthis.name = name;\n\t\t\tthis.value = new Value(initialValue);\n\t\t}\n\n\t\t@Override\n\t\tpublic AttributeDefinition getDefinition() {\n\t\t\treturn ")).output(mark("datamart", "FirstUpperCase")).output(literal("Struct.this.getDefinition().attribute(name).get();\n\t\t}\n\n\t\t@Override\n        public String name() {\n        \treturn name;\n        }\n\n\t\t@Override\n\t\tpublic Value value() {\n\t\t\treturn value;\n\t\t}\n\n\t\t@Override\n\t\tpublic void addChangeListener(ChangeListener listener) {}\n\n\t\t@Override\n\t\tpublic boolean equals(Object o) {\n\t\t\tif (this == o) return true;\n\t\t\tif (o == null || getClass() != o.getClass()) return false;\n\t\t\tAttribute other = (Attribute) o;\n\t\t\treturn name().equals(other.name()) && type().equals(other.type()) && value.equals(other.value());\n\t\t}\n\n\t\t@Override\n\t\tpublic int hashCode() {\n\t\t\treturn Objects.hash(name(), type(), value());\n\t\t}\n\n\t\t@Override\n\t\tpublic String toString() {\n\t\t\treturn type().getSimpleName() + \" \" + name() + \" = \" + value();\n\t\t}\n\t}\n\n\tprotected static final class StructDefinitionInternal implements StructDefinition {\n\n    \tprivate final String structName;\n\n    \tpublic StructDefinitionInternal(String structName) {\n    \t\tthis.structName = structName;\n    \t}\n\n    \t@Override\n    \tpublic String fullName() {\n    \t\treturn definition().fullName();\n    \t}\n\n    \t@Override\n    \tpublic List<AttributeDefinition> declaredAttributes() {\n    \t\treturn definition().declaredAttributes();\n    \t}\n\n    \t@Override\n    \tpublic java.util.Optional<StructDefinition> parent() {\n    \t\treturn definition().parent();\n    \t}\n\n    \t@Override\n    \tpublic List<StructDefinition> descendants() {\n    \t\treturn definition().descendants();\n    \t}\n\n    \t@Override\n    \tpublic Class<?> javaClass() {\n    \t\treturn definition().javaClass();\n    \t}\n\n    \t@Override\n    \tpublic boolean equals(Object o) {\n    \t\tif(o == null) return false;\n    \t\tif(o instanceof StructDefinition) return o.equals(definition());\n    \t\treturn false;\n    \t}\n\n    \t@Override\n    \tpublic int hashCode() {\n    \t\treturn definition().hashCode();\n    \t}\n\n    \tprivate StructDefinition definition() {\n    \t\treturn ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart.definition.struct(structName).orElseThrow(() -> new IllegalStateException(\"")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart is not initialized\"));\n    \t}\n    }\n}"))
		);
	}
}