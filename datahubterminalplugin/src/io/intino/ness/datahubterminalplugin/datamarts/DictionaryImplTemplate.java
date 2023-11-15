package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DictionaryImplTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("dictionary", "default")), (trigger("dictionaryimpl"))).output(literal("private static class DictionaryImpl implements Dictionary {\n\tprivate final String name;\n\tprivate final Map<String, WordImpl> words;\n\n\tprivate DictionaryImpl() {\n\t\tthis(\"\", new HashMap<>(0));\n\t}\n\n\tprivate DictionaryImpl(String name, Map<String, Map<String, String>> words) {\n\t\tthis.name = name;\n    \tthis.words = words.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new WordImpl(e.getKey(), e.getValue())));\n    }\n\n\t@Override\n\tpublic Dictionary.Word get(String wordId) {\n\t\tif(wordId == null) return Dictionary.Word.wrap(\"\");\n\t\tDictionary.Word word = words.get(wordId);\n\t\treturn word != null ? word : Dictionary.Word.wrap(wordId);\n\t}\n\n\t@Override\n\tpublic Stream<Dictionary.Word> words() {\n\t\tStream stream = words.values().stream();\n\t\treturn stream;\n\t}\n\n\t@Override\n\tpublic Set<String> languages() {\n\t\treturn words.values().stream().flatMap(w -> w.languages().stream()).collect(Collectors.toSet());\n\t}\n\n\tprivate static class WordImpl implements Dictionary.Word {\n\t\tprivate final String id;\n\t\tprivate final Map<String, String> translations;\n\n\t\tprivate WordImpl(String id, Map<String, String> translations) {\n\t\t\tthis.id = id;\n\t\t\tthis.translations = translations;\n\t\t}\n\n\t\t@Override\n\t\tpublic String get() {\n\t\t\treturn id;\n\t\t}\n\n        @Override\n        public Set<String> languages() {\n        \treturn translations.keySet();\n        }\n\n        @Override\n        public Optional<String> in(String language) {\n        \treturn Optional.ofNullable(translations.get(language));\n        }\n\t}\n}"))
		);
	}
}