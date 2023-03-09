package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityBaseTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("entity","base"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.util.Collection;\nimport java.util.Map;\nimport java.util.List;\nimport java.util.ArrayList;\nimport java.util.Objects;\n\nimport io.intino.ness.master.model.*;\nimport io.intino.ness.master.reflection.*;\nimport io.intino.ness.master.model.Concept.Attribute.Value;\n\npublic abstract class ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity implements Entity {\n\n\tprivate final String id;\n\tprivate final ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart;\n\tprivate final Map<String, Attribute> attributes;\n\tprivate List<ChangeListener> listeners;\n\n\tpublic ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity(String id, ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\tthis.id = id;\n\t\tthis.datamart = datamart;\n\t\tthis.attributes = createAllAttributes();\n\t}\n\n\t@Override\n\tpublic String id() {\n\t\treturn id;\n\t}\n\n\t@Override\n\tpublic boolean enabled() {\n\t\treturn attribute(\"enabled\").value().as(Boolean.class);\n\t}\n\n\t@Override\n\tpublic ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart datamart() {\n\t\treturn datamart;\n\t}\n\n\t@Override\n\tpublic Attribute attribute(String name) {\n\t\treturn attributes.get(name);\n\t}\n\n\t@Override\n\tpublic List<Concept.Attribute> attributes() {\n\t\treturn new ArrayList<>(attributes.values());\n\t}\n\n\t@Override\n\tpublic void addChangeListener(ChangeListener listener) {\n\t\tif(listener == null) throw new NullPointerException(\"ChangeListener cannot be null\");\n\t\tif(listeners == null) listeners = new ArrayList<>(1);\n\t\tlisteners.add(listener);\n\t}\n\n\tvoid updateAttribute(String name, Object newValue) {\n\t\tAttribute attribute = attribute(name);\n\t\tValue oldValue = attribute.value();\n\t\tattribute.setValue(newValue);\n\t\tif(!oldValue.equals(attribute.value())) notifyChangeListeners(attribute, oldValue);\n\t}\n\n\tprivate void notifyChangeListeners(Attribute attribute, Value oldValue) {\n\t\tif(listeners == null) return;\n\t\tfor(ChangeListener listener : listeners) {\n\t\t\ttry {listener.onChange(this, attribute, oldValue);} catch(Throwable e) {}\n\t\t}\n\t}\n\n\t@Override\n\tpublic boolean equals(Object o) {\n\t\tif (this == o) return true;\n\t\tif (o == null || getClass() != o.getClass()) return false;\n\t\treturn id.equals(((Entity) o).id());\n\t}\n\n\t@Override\n\tpublic int hashCode() {\n\t\treturn id.hashCode();\n\t}\n\n\t@Override\n\tpublic String toString() {\n\t\treturn id;\n\t}\n\n\tprotected Collection<Attribute> initDeclaredAttributes() {\n\t\tList<Attribute> attributes = new ArrayList<>();\n\t\tattributes.add(new Attribute(\"id\", id));\n\t\tattributes.add(new Attribute(\"enabled\", true));\n\t\treturn attributes;\n\t}\n\n\tprivate Map<String, Attribute> createAllAttributes() {\n\t\tCollection<Attribute> declaredAttribs = initDeclaredAttributes();\n\t\tMap<String, Attribute> attributes = new java.util.LinkedHashMap<>(2 + declaredAttribs.size());\n        declaredAttribs.forEach(attr -> attributes.put(attr.name(), attr));\n        return attributes;\n\t}\n\n\tprotected final class Attribute implements Concept.Attribute {\n\n\t\tprivate final String name;\n\t\tprivate Value value;\n\t\tprivate List<ChangeListener> listeners;\n\n\t\tpublic Attribute(String name) {\n        \tthis(name, null);\n        }\n\n\t\tpublic Attribute(String name, Object initialValue) {\n\t\t\tthis.name = name;\n\t\t\tthis.value = new Value(initialValue);\n\t\t}\n\n\t\t@Override\n\t\tpublic AttributeDefinition getDefinition() {\n\t\t\treturn ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity.this.getDefinition().attribute(name).get();\n\t\t}\n\n\t\t@Override\n        public String name() {\n        \treturn name;\n        }\n\n\t\t@Override\n\t\tpublic Value value() {\n\t\t\treturn value;\n\t\t}\n\n\t\t@Override\n\t\tpublic void addChangeListener(ChangeListener listener) {\n\t\t\tif(listener == null) throw new NullPointerException(\"ChangeListener cannot be null\");\n\t\t\tif(listeners == null) listeners = new ArrayList<>(1);\n\t\t\tlisteners.add(listener);\n\t\t}\n\n\t\tprivate void setValue(Object newValue) {\n\t\t\tValue oldValue = this.value;\n\t\t\tthis.value = new Value(newValue);\n\t\t\tif(!oldValue.equals(newValue)) notifyChangeListeners(oldValue, this.value);\n\t\t}\n\n\t\tprivate void notifyChangeListeners(Value oldValue, Value newValue) {\n\t\t\tif(listeners == null) return;\n\t\t\tfor(ChangeListener listener : listeners) {\n\t\t\t\ttry {\n\t\t\t\t\tlistener.onValueChange(oldValue, newValue);\n\t\t\t\t} catch(Throwable ignored) {}\n\t\t\t}\n\t\t}\n\n\t\t@Override\n\t\tpublic boolean equals(Object o) {\n\t\t\tif (this == o) return true;\n\t\t\tif (o == null || getClass() != o.getClass()) return false;\n\t\t\tAttribute other = (Attribute) o;\n\t\t\treturn name().equals(other.name()) && type().equals(other.type()) && value.equals(other.value());\n\t\t}\n\n\t\t@Override\n\t\tpublic int hashCode() {\n\t\t\treturn Objects.hash(name(), type(), value());\n\t\t}\n\n\t\t@Override\n\t\tpublic String toString() {\n\t\t\treturn type().getSimpleName() + \" \" + name() + \" = \" + value();\n\t\t}\n\t}\n\n\tprotected static final class EntityDefinitionInternal implements EntityDefinition {\n\n    \tprivate final String entityName;\n    \tprivate EntityDefinition definition;\n\n    \tpublic EntityDefinitionInternal(String entityName) {\n    \t\tthis.entityName = entityName;\n    \t}\n\n    \t@Override\n    \tpublic boolean isAbstract() {\n    \t\treturn definition().isAbstract();\n    \t}\n\n    \t@Override\n    \tpublic String fullName() {\n    \t\treturn definition().fullName();\n    \t}\n\n    \t@Override\n    \tpublic List<AttributeDefinition> declaredAttributes() {\n    \t\treturn definition().declaredAttributes();\n    \t}\n\n    \t@Override\n    \tpublic java.util.Optional<EntityDefinition> parent() {\n    \t\treturn definition().parent();\n    \t}\n\n    \t@Override\n    \tpublic List<EntityDefinition> descendants() {\n    \t\treturn definition().descendants();\n    \t}\n\n    \t@Override\n    \tpublic Class<?> javaClass() {\n    \t\treturn definition().javaClass();\n    \t}\n\n    \t@Override\n    \tpublic boolean equals(Object o) {\n    \t\tif(o == null) return false;\n    \t\tif(o instanceof EntityDefinition) return o.equals(definition());\n    \t\treturn false;\n    \t}\n\n    \t@Override\n    \tpublic int hashCode() {\n    \t\treturn java.util.Objects.hash(definition());\n    \t}\n\n    \tprivate EntityDefinition definition() {\n    \t\tif(definition == null) {\n    \t\t\tdefinition = ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart.definition.entity(entityName).orElseThrow(() -> new IllegalStateException(\"")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart is not initialized\"));\n    \t\t}\n    \t\treturn definition;\n    \t}\n    }\n}"))
		);
	}
}