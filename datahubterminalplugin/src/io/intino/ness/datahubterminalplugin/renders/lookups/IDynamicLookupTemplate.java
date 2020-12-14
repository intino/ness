package io.intino.ness.datahubterminalplugin.renders.lookups;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class IDynamicLookupTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\npublic interface DynamicLookup {\n\tpublic void open();\n\tpublic void commit();\n\tpublic void close();\n}"))
		);
	}
}