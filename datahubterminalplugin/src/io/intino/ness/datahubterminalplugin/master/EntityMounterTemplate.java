package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntityMounterTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("mounter","interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\npublic interface ")).output(mark("datamart", "FirstUpperCase")).output(literal("Mounter {\n\n\tvoid mount(io.intino.alexandria.event.Event event);\n\n\tdefault void update(")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity entity, String attribute, Object value) {\n\t\tentity.updateAttribute(attribute, value);\n\t}\n\n\tenum Operation {\n\t\tCreate, Update, Remove, Skip\n\t}\n}")),
			rule().condition(not(type("abstract")), (type("mounter")), (type("message"))).output(literal("package ")).output(mark("package")).output(literal(".mounters;\n\nimport io.intino.ness.master.Datamart.EntityListener;\nimport ")).output(mark("ontologypackage")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart;\n\nimport java.time.*;\nimport java.util.*;\nimport java.util.stream.*;\n\nimport io.intino.alexandria.event.Event;\nimport io.intino.alexandria.event.message.MessageEvent;\nimport io.intino.alexandria.message.Message;\nimport ")).output(mark("ontologypackage")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity;\n\nimport static ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Mounter.Operation.*;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal("Mounter implements ")).output(mark("package")).output(literal(".")).output(mark("datamart", "FirstUpperCase")).output(literal("Mounter {\n\n\tprivate final ")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart.Entities entities;\n\tprivate final List<EntityListener> listeners;\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("Mounter(")).output(mark("datamart", "FirstUpperCase")).output(literal("Datamart.Entities entities, List<EntityListener> listeners) {\n\t\tthis.entities = entities;\n\t\tthis.listeners = listeners;\n\t}\n\n\t@Override\n\tpublic void mount(Event event) {\n\t\tMessage message = ((MessageEvent)event).toMessage();\n\t\tString id = message.get(\"id\").asString();\n\t\tOperation[] operation = {Update};\n\t\t")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity entity = findOrCreateEntity(id, operation);\n\t\tupdateAttributes(message, operation, entity);\n\t\tif(operation[0] != Skip) notifyListeners(operation[0], entity);\n\t}\n\n\tprivate void updateAttributes(Message message, Operation[] operation, ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity entity) {\n\t\tfor(String attr : message.attributes()) {\n\t\t\tupdate(entity, attr, parse(attr, message));\n\t\t\tif(attr.equals(\"enabled\") && !message.get(\"enabled\").asBoolean()) {\n\t\t\t\toperation[0] = operation[0] == Create ? Skip : Remove;\n\t\t\t\tentities.remove(entity.id());\n\t\t\t\tbreak;\n\t\t\t}\n\t\t}\n\t}\n\n\tprivate ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity findOrCreateEntity(String id, Operation[] operation) {\n\t\t")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity entity = entities.get(")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(".definition, id);\n\t\tif(entity == null) {\n\t\t\tentity = new ")).output(mark("ontologypackage")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("(id, entities.datamart());\n\t\t\tentities.add(entity);\n\t\t\toperation[0] = Create;\n\t\t}\n\t\treturn entity;\n\t}\n\n\tprivate void notifyListeners(Operation operation, ")).output(mark("datamart", "FirstUpperCase")).output(literal("Entity entity) {\n\t\tfor(EntityListener listener : listeners) {\n\t\t\tswitch(operation) {\n\t\t\t\tcase Create: listener.onCreate(entity); break;\n\t\t\t\tcase Update: listener.onUpdate(entity); break;\n\t\t\t\tcase Remove: listener.onRemove(entity); break;\n\t\t\t}\n\t\t}\n\t}\n\n\tprivate Object parse(String attribute, Message message) {\n\t\tif(message.get(attribute).isNull()) return null;\n\t\tswitch(attribute) {\n\t\t\t")).output(expression().output(mark("attribute", "parseSwitchCase").multiple("\n"))).output(literal("\n\t\t}\n\t\treturn null;\n\t}\n\n\t")).output(expression().output(mark("attribute", "parseMethod").multiple("\n\n"))).output(literal("\n}")),
			rule().condition((trigger("parseswitchcase"))).output(literal("case \"")).output(mark("name")).output(literal("\": return parse")).output(mark("name", "FirstUpperCase")).output(literal("(message);")),
			rule().condition((type("attribute")), (type("list")), (type("entity")), (trigger("parsemethod"))).output(literal("private java.util.List<String> parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn java.util.Arrays.asList(m.get(\"")).output(mark("name")).output(literal("\").as(String[].class));\n}")),
			rule().condition((type("attribute")), (type("set")), (type("entity")), (trigger("parsemethod"))).output(literal("private java.util.Set<String> parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn java.util.Set.of(m.get(\"")).output(mark("name")).output(literal("\").as(String[].class));\n}")),
			rule().condition((type("attribute")), (anyTypes("list","set")), (anyTypes("date","datetime")), (trigger("parsemethod"))).output(literal("private static final java.time.format.DateTimeFormatter ")).output(mark("name", "FirstUpperCase")).output(literal("Formatter = java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\");\nprivate ")).output(mark("type")).output(literal(" parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\ttry {\n\t\treturn java.util.Arrays.stream(m.get(\"")).output(mark("name")).output(literal("\").as(String[].class))\n\t\t\t.map(s -> {try{return ")).output(mark("typeParameter")).output(literal(".parse(s, ")).output(mark("name", "FirstUpperCase")).output(literal("Formatter);}catch(Exception ignored){return null;}})\n\t\t\t.filter(java.util.Objects::nonNull)\n\t\t\t.collect(java.util.stream.Collectors.to")).output(mark("collectionType", "FirstUpperCase")).output(literal("());\n\t} catch(Exception ignored) {\n\t\treturn ")).output(mark("defaultValue")).output(literal(";\n\t}\n}")),
			rule().condition((type("attribute")), (type("list")), (trigger("parsemethod"))).output(literal("private java.util.List<")).output(mark("typeParameter")).output(literal("> parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\tvar value = m.get(\"")).output(mark("name")).output(literal("\");\n\treturn value.data().isEmpty() ? java.util.Collections.emptyList() : value.asList(")).output(mark("typeParameter")).output(literal(".class);\n}")),
			rule().condition((type("attribute")), (type("set")), (trigger("parsemethod"))).output(literal("private java.util.Set<")).output(mark("typeParameter")).output(literal("> parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\tvar value = m.get(\"")).output(mark("name")).output(literal("\");\n\treturn value.data().isEmpty() ? java.util.Collections.emptySet() : value.asSet(")).output(mark("typeParameter")).output(literal(".class);\n}")),
			rule().condition((type("attribute")), (type("entity")), (trigger("parsemethod"))).output(literal("private String parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn m.get(\"")).output(mark("name")).output(literal("\").asString();\n}")),
			rule().condition((type("attribute")), (type("struct")), (trigger("parsemethod"))).output(literal("private ")).output(mark("type")).output(literal(" parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn m.get(\"")).output(mark("name")).output(literal("\").as(")).output(mark("type")).output(literal(".class);\n}")),
			rule().condition((type("attribute")), (type("map")), (trigger("parsemethod"))).output(literal("private java.util.Map<String, String> parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn m.get(\"")).output(mark("name")).output(literal("\").as(java.util.Map.class);\n}")),
			rule().condition((type("attribute")), (anyTypes("date","datetime")), (trigger("parsemethod"))).output(literal("private static final java.time.format.DateTimeFormatter ")).output(mark("name", "FirstUpperCase")).output(literal("Formatter = java.time.format.DateTimeFormatter.ofPattern(\"")).output(mark("format")).output(literal("\");\nprivate ")).output(mark("type")).output(literal(" parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\ttry {\n\t\treturn ")).output(mark("type")).output(literal(".parse((m.get(\"")).output(mark("name")).output(literal("\").asString()), ")).output(mark("name", "FirstUpperCase")).output(literal("Formatter);\n\t} catch(Exception ignored) {\n\t\treturn ")).output(mark("defaultValue")).output(literal(";\n\t}\n}")),
			rule().condition((type("attribute")), (type("word")), (trigger("parsemethod"))).output(literal("private ")).output(mark("type")).output(literal(" parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\ttry {\n\t\treturn ")).output(mark("type")).output(literal(".valueOf(m.get(\"")).output(mark("name")).output(literal("\").asString());\n\t} catch(Exception ignored) {\n\t\treturn ")).output(mark("defaultValue")).output(literal(";\n\t}\n}")),
			rule().condition((type("attribute")), (trigger("parsemethod"))).output(literal("private ")).output(mark("type")).output(literal(" parse")).output(mark("name", "FirstUpperCase")).output(literal("(Message m) {\n\treturn m.get(\"")).output(mark("name")).output(literal("\").as(")).output(mark("type")).output(literal(".class);\n}"))
		);
	}
}