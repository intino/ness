package io.intino.ness.datahubterminalplugin;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("terminal"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport ")).output(mark("package", "validPackage")).output(literal(".schemas.*;\nimport java.util.List;\n\npublic class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\n\tprivate final io.intino.alexandria.message.MessageHub messageHub;\n\n\tpublic static String[] subscriptionChannels = new String[]{")).output(mark("subscribe", "channel")).output(literal("};\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.message.MessageHub messageHub) {\n\t\tthis.messageHub = messageHub;\n\t}\n\n\tpublic io.intino.alexandria.message.MessageHub messageHub() {\n\t\treturn this.messageHub;\n\t}\n\n\t")).output(mark("publish").multiple("\n\n")).output(literal("\n\n\t")).output(mark("subscribe").multiple("\n\n")).output(literal("\n\n\t")).output(mark("message", "interface").multiple("\n\n")).output(literal("\n\n\tpublic void stop() {\n\t\t//messageHub.stop();\n\t}\n}")),
				rule().condition((type("multicontext")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("type", "firstLowerCase")).output(literal(", ")).output(mark("type", "FirstUpperCase")).output(literal(".Context... contexts) {\n\tfor (")).output(mark("type", "FirstUpperCase")).output(literal(".Context c : contexts)\n\t\tmessageHub.sendMessage(c.qn() + \".")).output(mark("type")).output(literal("\", ")).output(mark("type", "firstLowerCase")).output(literal(".get());\n}")),
				rule().condition((trigger("publish"))).output(literal("public void publish(")).output(mark("type", "FirstUpperCase")).output(literal(" ")).output(mark("type", "firstLowerCase")).output(literal(") {\n\tmessageHub.sendMessage(\"")).output(mark("channel")).output(literal("\", ")).output(mark("type", "firstLowerCase")).output(literal(".get());\n}")),
				rule().condition((type("multiContext")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("type", "FirstUpperCase")).output(literal("Consumer onEventReceived, ")).output(mark("type", "FirstUpperCase")).output(literal(".Context... contexts) {\n\tfor (")).output(mark("type", "FirstUpperCase")).output(literal(".Context c : contexts)\n\t\tmessageHub.attachListener(c.qn() + \".")).output(mark("type")).output(literal("\", m -> onEventReceived.accept(new ")).output(mark("type", "FirstUpperCase")).output(literal("(m)));\n}")),
				rule().condition((trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("type", "FirstUpperCase")).output(literal("Consumer onEventReceived) {\n\tmessageHub.attachListener(\"")).output(mark("channel")).output(literal("\", m -> onEventReceived.accept(new ")).output(mark("type", "FirstUpperCase")).output(literal("(m)));\n}")),
				rule().condition((trigger("quoted"))).output(literal("\"")).output(mark("")).output(literal("\"")),
				rule().condition((trigger("interface"))).output(literal("public interface ")).output(mark("name")).output(literal("Consumer extends java.util.function.Consumer<")).output(mark("name", "FirstUpperCase")).output(literal("> {\n}"))
		);
	}
}