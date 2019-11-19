package io.intino.ness.datahubaccessorplugin;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DataHubTerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("accessor"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport ")).output(mark("package", "validPackage")).output(literal(".schemas.*;\nimport java.util.List;\n\npublic class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\n\tprivate final io.intino.alexandria.message.MessageHub messageHub;\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.message.MessageHub messageHub) {\n\t\tthis.messageHub = messageHub;\n\t}\n\n\t")).output(mark("publish").multiple("\n\n")).output(literal("\n\n\t")).output(mark("subscribe").multiple("\n\n")).output(literal("\n}")),
				rule().condition((type("contextual")), (trigger("publish"))).output(mark("context", "publish").multiple("\n\n")),
				rule().condition((type("context")), (trigger("publish"))).output(literal("public void sendTo")).output(mark("context")).output(literal("(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("type", "firstLowerCase")).output(literal(") {\n\tmessageHub.sendMessage(\"")).output(mark("channel")).output(literal("\", ")).output(mark("type", "firstLowerCase")).output(literal(".get());\n}")),
				rule().condition((type("contextual")), (trigger("subscribe"))).output(mark("context", "subscribe").multiple("\n\n")),
				rule().condition((type("context")), (trigger("subscribe"))).output(literal("public void subscribeTo")).output(mark("context", "FirstUpperCase")).output(mark("type", "FirstUpperCase")).output(literal("(java.util.function.Consumer<")).output(mark("type", "FirstUpperCase")).output(literal("> onMessageReceived) {\n\tmessageHub.attachListener(\"")).output(mark("channel")).output(literal("\", m -> onMessageReceived.accept(new ")).output(mark("type", "FirstUpperCase")).output(literal("(m)));\n}")),
				rule().condition((trigger("subscribe"))).output(literal("public void subscribeTo")).output(mark("type", "FirstUpperCase")).output(mark("type", "FirstUpperCase")).output(literal("(java.util.function.Consumer<")).output(mark("type", "FirstUpperCase")).output(literal("> onMessageReceived) {\n\tmessageHub.attachListener(\"")).output(mark("channel")).output(literal("\", m -> onMessageReceived.accept(new ")).output(mark("type", "FirstUpperCase")).output(literal("(m)));\n}")),
				rule().condition((trigger("publish"))).output(literal("public void send(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("type", "firstLowerCase")).output(literal(") {\n\tmessageHub.sendMessage(\"")).output(mark("channel")).output(literal("\", ")).output(mark("type", "firstLowerCase")).output(literal(".get());\n}"))
		);
	}
}