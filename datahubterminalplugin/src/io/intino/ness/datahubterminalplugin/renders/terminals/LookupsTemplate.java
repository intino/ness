package io.intino.ness.datahubterminalplugin.renders.terminals;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class LookupsTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("lookups"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\npublic class Lookups {\n\tprivate final java.io.File lookupsDirectory;\n\t")).output(mark("namespace").multiple("\n")).output(literal("\n\t")).output(mark("lookup", "field").multiple("\n")).output(literal("\n\n\tpublic Lookups(java.io.File lookupsDirectory) {\n\t\tthis.lookupsDirectory = lookupsDirectory;\n\t}\n\n\t")).output(mark("lookup", "getter").multiple("\n")).output(literal("\n}")),
			rule().condition((trigger("namespace"))).output(literal("private java.sql.Connection ")).output(mark("", "firstLowerCase")).output(literal(";")),
			rule().condition((type("lookup")), (trigger("field"))).output(literal("private ")).output(mark("qn")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal(";")),
			rule().condition((type("lookup")), (trigger("getter"))).output(literal("public ")).output(mark("qn")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tif (this.")).output(mark("namespace", "firstLowerCase")).output(literal(" == null) {\n\t\ttry {\n\t\t\tthis.")).output(mark("namespace", "firstLowerCase")).output(literal(" = java.sql.DriverManager.getConnection(\"jdbc:sqlite:\" + new java.io.File(lookupsDirectory,  \"")).output(mark("namespace", "firstLowerCase")).output(literal(".db\"));\n    \t\tthis.")).output(mark("namespace", "firstLowerCase")).output(literal(".setAutoCommit(false);\n\t\t} catch (java.sql.SQLException e) {\n\t\t\tio.intino.alexandria.logger.Logger.error(e);\n\t\t}\n\t}\n\tif (this.")).output(mark("name", "firstLowerCase")).output(literal(" == null) this.")).output(mark("name", "firstLowerCase")).output(literal(" = new ")).output(mark("qn")).output(literal("(")).output(mark("namespace", "firstLowerCase")).output(literal(");\n\treturn this.")).output(mark("name", "firstLowerCase")).output(literal(";\n}"))
		);
	}
}