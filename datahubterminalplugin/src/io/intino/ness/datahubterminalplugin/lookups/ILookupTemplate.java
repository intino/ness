package io.intino.ness.datahubterminalplugin.lookups;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ILookupTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\npublic interface DynamicLookup {\n\tvoid open();\n\tvoid commit();\n\tvoid close();\n}"))
		);
	}
}