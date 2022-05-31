package io.intino.ness.datahubterminalplugin;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DatalakeTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("datalake"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport io.intino.alexandria.logger.Logger;\n\nimport java.io.File;\nimport io.intino.alexandria.datalake.Datalake.EventStore.Tank;\nimport io.intino.alexandria.datalake.file.FileDatalake;\n\npublic class Datalake {\n\tprivate final io.intino.alexandria.datalake.Datalake datalake;\n\n\tpublic Datalake(File file) {\n\t\tthis.datalake = new FileDatalake(file);\n\t}\n\n\t")).output(mark("tank").multiple("\n\n")).output(literal("\n}")),
			rule().condition((trigger("tank"))).output(literal("public Tank ")).output(mark("name")).output(literal("() {\n\treturn this.datalake.eventStore().tank(\"")).output(mark("qn")).output(literal("\");\n}"))
		);
	}
}