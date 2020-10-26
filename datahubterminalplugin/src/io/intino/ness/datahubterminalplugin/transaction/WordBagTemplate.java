package io.intino.ness.datahubterminalplugin.transaction;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class WordBagTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("root"))).output(literal("package ")).output(mark("package", "ValidPackage")).output(literal(";\n\nimport java.util.ArrayList;\nimport java.util.List;\nimport java.util.function.Predicate;\nimport java.util.stream.Collectors;\n\n")).output(mark("wordBag")),
			rule().condition((allTypes("wordBag","resource"))).output(literal("public class ")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\tprivate static final java.util.Map<Integer, Word> words;\n\tprivate static final java.util.Map<String, Word> wordsByName;\n\n\tstatic {\n\t\twords = new java.util.HashMap<>();\n\t\twordsByName = new java.util.HashMap<>();\n\t\ttry (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(Municipios.class.getResourceAsStream(\"/")).output(mark("resource")).output(literal("\")))) {\n\t\t\treader.lines().map(l -> l.split(\"\\t\")).\n\t\t\tmap(l -> new Word(")).output(mark("column", "constructor").multiple(", ")).output(literal(")).\n\t\t\tforEach(w -> {words.put(w.index, w); wordsByName.put(w.name, w);});\n\t\t} catch (java.io.IOException e) {\n\t\t\tio.intino.alexandria.logger.Logger.error(e);\n\t\t}\n\t}\n\n\tpublic static List<Word> words() {\n\t\treturn new ArrayList<>(words.values());\n\t}\n\n\tpublic static List<Word> words(Predicate<Word> filter) {\n\t\treturn words.values().stream().filter(filter).collect(Collectors.toList());\n\t}\n\n\tpublic static Word wordByIndex(int index) {\n\t\treturn words.get(index);\n\t}\n\n\tpublic static Word wordByName(String name) {\n\t\treturn wordsByName.get(name);\n\t}\n\n\t")).output(mark("column", "find").multiple("\n\n")).output(literal("\n\n\tpublic static class Word {\n\t\t")).output(mark("column", "declaration").multiple("\n")).output(literal("\n\n\t\tWord(")).output(mark("column", "parameter").multiple(", ")).output(literal(") {\n\t\t\t")).output(mark("column", "assign").multiple("\n")).output(literal("\n\t\t}\n\t}\n}")),
			rule().condition((type("wordBag"))).output(literal("public class ")).output(mark("name", "firstUpperCase")).output(literal(" {\n\tprivate static final java.util.Map<Short, Word> words;\n\tprivate static final java.util.Map<String, Word> wordsByName;\n\n\tstatic {\n\t\twords = new java.util.HashMap<>();\n\t\twordsByName = new java.util.HashMap<>();\n\t\tWord w;\n\t\t")).output(mark("word", "put").multiple("\n")).output(literal("\n\t}\n\n\tpublic static List<Word> words() {\n\t\treturn new ArrayList<>(words.values());\n\t}\n\n\tpublic static List<Word> words(Predicate<Word> filter) {\n\t\treturn words.values().stream().filter(filter).collect(Collectors.toList());\n\t}\n\n\tpublic static Word wordByIndex(short index) {\n\t\treturn words.get((short)index);\n\t}\n\n\tpublic static Word wordByName(String name) {\n\t\treturn wordsByName.get(name);\n\t}\n\n\tpublic static Word wordByLabel(String label) {\n\t\treturn words.values().stream().filter(c -> c.label.equals(label)).findFirst().orElse(new Word((short) 0, null));\n\t}\n\n\tpublic static class Word {\n\t\tpublic final short index;\n\t\tpublic final String name;\n\t\tpublic final String label;\n\n\t\tWord(short index, String name) {\n\t\t\tthis(index, name, name);\n\t\t}\n\n\t\tWord(short index, String name, String label) {\n\t\t\tthis.index = index;\n\t\t\tthis.name = name;\n\t\t\tthis.label = label;\n\t\t}\n\t}\n}")),
			rule().condition((trigger("declaration"))).output(literal("public final ")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((trigger("parameter"))).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")),
			rule().condition((anyTypes("string","text")), (trigger("constructor"))).output(literal("l[")).output(mark("index")).output(literal("]")),
			rule().condition((type("integer")), (trigger("constructor"))).output(literal("Integer.parseInt(l[")).output(mark("index")).output(literal("])")),
			rule().condition((type("real")), (trigger("constructor"))).output(literal("Double.parseDouble(l[")).output(mark("index")).output(literal("])")),
			rule().condition((anyTypes("longInteger","long")), (trigger("constructor"))).output(literal("Long.parseLong(l[")).output(mark("index")).output(literal("])")),
			rule().condition((anyTypes("boolean","bool")), (trigger("constructor"))).output(literal("Boolean.parseBoolean(l[")).output(mark("index")).output(literal("])")),
			rule().condition((trigger("assign"))).output(literal("this.")).output(mark("name", "firstLowerCase")).output(literal(" = ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("primitive")), not(attribute("index", "0")), not(attribute("index", "1")), (trigger("find"))).output(literal("public static Word wordBy")).output(mark("name", "firstUpperCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\treturn words.values().stream().filter(c -> c.")).output(mark("name", "firstLowerCase")).output(literal(" == ")).output(mark("name", "firstLowerCase")).output(literal(").findFirst().orElse(null);\n}")),
			rule().condition(not(attribute("index", "0")), not(attribute("index", "1")), (trigger("find"))).output(literal("public static Word wordBy")).output(mark("name", "firstUpperCase")).output(literal("(")).output(mark("type")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(") {\n\treturn words.values().stream().filter(c -> c.")).output(mark("name", "firstLowerCase")).output(literal(".equals(")).output(mark("name", "firstLowerCase")).output(literal(")).findFirst().orElse(null);\n}")),
			rule().condition((trigger("put"))).output(literal("w = new Word((short) ")).output(mark("index")).output(literal(", \"")).output(mark("name")).output(literal("\"")).output(expression().output(literal(", \"")).output(mark("label")).output(literal("\""))).output(literal(");\nwords.put((short) ")).output(mark("index")).output(literal(", w);\nwordsByName.put(\"")).output(mark("name")).output(literal("\", w);"))
		);
	}
}