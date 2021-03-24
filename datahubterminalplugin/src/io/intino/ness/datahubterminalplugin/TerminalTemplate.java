package .Users.oroncal.workspace.ness.datahubterminalplugin.src.io.intino.ness.datahubterminalplugin;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("terminal"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport io.intino.alexandria.Timetag;\nimport io.intino.alexandria.Scale;\nimport io.intino.alexandria.event.Event;\n\nimport java.util.function.Consumer;\nimport java.util.List;\n\npublic class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\tprivate static final Scale scale = Scale.")).output(mark("scale")).output(literal(";\n\tprivate final io.intino.alexandria.terminal.Connector connector;\n\tprivate java.util.Map<java.util.function.Consumer<?>, java.util.function.Consumer<io.intino.alexandria.event.Event>> consumers = new java.util.HashMap<>();\n\t")).output(mark("bpm", "splits")).output(literal("\n\tpublic static String[] subscriptionChannels = new String[]{")).output(mark("subscribe", "channel")).output(literal("};\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.terminal.Connector connector) {\n\t\tthis.connector = connector;\n\t}\n\n\tpublic void publish(Object event, String split) {\n\t\t")).output(mark("publish", "if").multiple("\n")).output(literal("\n\t}\n\n\t")).output(expression().output(mark("datalake")).output(literal("\n")).output(literal("public BatchSession batch(java.io.File dataHubStageDirectory, java.io.File temporalStageDirectory) {")).output(literal("\n")).output(literal("\treturn new BatchSession(dataHubStageDirectory, temporalStageDirectory);")).output(literal("\n")).output(literal("}")).output(literal("\n")).output(literal("\n")).output(literal("public BatchSession batch(java.io.File dataHubStageDirectory, java.io.File temporalStageDirectory, Config config) {")).output(literal("\n")).output(literal("\treturn new BatchSession(dataHubStageDirectory, temporalStageDirectory, config);")).output(literal("\n")).output(literal("}")).output(literal("\n")).output(literal(""))).output(literal("\n\n\tpublic void publish(io.intino.alexandria.event.SessionEvent session) {\n\t\tconnector.sendEvent(io.intino.alexandria.event.SessionEvent.PATH, session);\n\t}\n\n\tpublic void subscribe(SessionEventConsumer onEventReceived) {\n\t\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new io.intino.alexandria.event.SessionEvent(event.toMessage())));\n\t\tconnector.attachListener(io.intino.alexandria.event.SessionEvent.PATH, consumers.get(onEventReceived));\n\t}\n\n\t")).output(mark("publish").multiple("\n\n")).output(literal("\n\n\t")).output(mark("subscribe").multiple("\n\n")).output(literal("\n\n\tprivate static final Object monitor = new Object();\n\n\tpublic synchronized void requestSeal() {\n\t\tsynchronized(monitor) {\n\t\t\tconnector.requestResponse(\"service.ness.seal\", new Event(new io.intino.alexandria.message.Message(\"Seal\").ts(java.time.Instant.now())).toString(), s -> {\n\t\t\t\t\tsynchronized(monitor) {\n\t\t\t\t\t\tmonitor.notify();\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t);\n\t\t\ttry {\n\t\t\t\tmonitor.wait(1000*60*30);\n\t\t\t} catch (InterruptedException e) {\n\t\t\t\tio.intino.alexandria.logger.Logger.error(e);\n\t\t\t}\n\t\t}\n\t}\n\n\tpublic class BatchSession {\n\t\tprivate final java.io.File dataHubStage;\n\t\tprivate final java.io.File temporalStage;\n\t\tprivate final io.intino.alexandria.ingestion.SessionHandler sessionHandler;\n\t\tprivate final io.intino.alexandria.ingestion.EventSession eventSession;\n\t\tprivate final io.intino.alexandria.ingestion.SetSession setSession;\n\n\t\tpublic BatchSession(java.io.File dataHubStage, java.io.File temporalStage) {\n\t\t\tthis(dataHubStage, temporalStage, new Config());\n\t\t}\n\n\t\tpublic BatchSession(java.io.File dataHubStage, java.io.File temporalStage, Config config) {\n\t\t\tthis.dataHubStage = dataHubStage;\n\t\t\tthis.temporalStage = temporalStage;\n\t\t\tthis.sessionHandler = new io.intino.alexandria.ingestion.SessionHandler(temporalStage);\n\t\t\tthis.eventSession = sessionHandler.createEventSession();\n\t\t\tthis.setSession = sessionHandler.createSetSession(config.setsBufferSise);\n\t\t}\n\n\t\tpublic void feed(Event event, String split) {\n            eventSession.put(tankOf(event, split), Timetag.of(event.ts(), scale), event);\n\t\t}\n\n\t\tpublic void feed(io.intino.alexandria.event.SessionEvent event) {\n\t\t\teventSession.put(io.intino.alexandria.event.SessionEvent.PATH, Timetag.of(event.ts(), Scale.Day), event);\n\t\t}\n\n\t\tpublic void flush() {\n\t\t\teventSession.flush();\n\t\t\tsetSession.flush();\n\t\t}\n\n\t\tpublic void push() {\n\t\t\teventSession.close();\n\t\t\tsetSession.close();\n\t\t\tsessionHandler.pushTo(this.dataHubStage);\n\t\t\t//connector.sendEvent(\"service.ness.push\", new Event(new io.intino.alexandria.message.Message(\"Push\").set(\"stage\", temporalStage.getName())));\n\t\t}\n\n\t\tpublic synchronized void seal() {\n\t\t\tsynchronized(monitor) {\n\t\t\t\tconnector.requestResponse(\"service.ness.seal\", new Event(new io.intino.alexandria.message.Message(\"Seal\").set(\"stage\", temporalStage.getName())).ts(java.time.Instant.now()).toString(), s -> {\n\t\t\t\t\t\tsynchronized(monitor) {\n\t\t\t\t\t\t\tmonitor.notify();\n\t\t\t\t\t\t}\n\t\t\t\t\t}\n\t\t\t\t);\n\t\t\t\ttry {\n\t\t\t\t\tmonitor.wait();\n\t\t\t\t} catch (InterruptedException e) {\n\t\t\t\t\tio.intino.alexandria.logger.Logger.error(e);\n\t\t\t\t}\n\t\t\t}\n        }\n\n        private String tankOf(Event event, String split) {\n        \t")).output(mark("publish", "tankOf").multiple("\n")).output(literal("\n        \treturn event.toMessage().type();\n        }\n\t}\n\n\tpublic static class Config {\n\t\tprivate int eventsBufferSise = 1_000_000;\n\t\tprivate int setsBufferSise = 1_000_000;\n\n\t\tpublic Config eventsBufferSise(int eventsBufferSise) {\n\t\t\tthis.eventsBufferSise = eventsBufferSise;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic Config setsBufferSise(int setsBufferSise) {\n\t\t\tthis.setsBufferSise = setsBufferSise;\n\t\t\treturn this;\n\t\t}\n\t}\n\n\tpublic interface SessionEventConsumer extends java.util.function.Consumer<io.intino.alexandria.event.SessionEvent> {\n\t}\n\n\t")).output(mark("event", "interface").multiple("\n\n")).output(literal("\n}")),
			rule().condition((type("bpm")), (trigger("splits"))).output(literal("public enum BpmSplit {\n\t")).output(mark("split", "asEnum").multiple(", ")).output(literal(";\n\n\tpublic abstract String qn();\n\n\tpublic static BpmSplit splitByQn(String qn) {\n\t\treturn java.util.Arrays.stream(values()).filter(c -> c.qn().equals(qn)).findFirst().orElse(null);\n\t}\n}")),
			rule().condition((trigger("asenum"))).output(mark("value", "snakeCaseToCamelCase")).output(literal(" {\n\tpublic String qn() {\n\t\treturn \"")).output(mark("qn")).output(literal("\";\n\t}\n}")),
			rule().condition((allTypes("multisplit","bpm")), (trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") publish((")).output(mark("type")).output(literal(") event, BpmSplit.splitByQn(split));")),
			rule().condition((type("multisplit")), (trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") publish((")).output(mark("type")).output(literal(") event, ")).output(mark("type")).output(literal(".Split.splitByQn(split));")),
			rule().condition((trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") publish((")).output(mark("type")).output(literal(") event);")),
			rule().condition((type("multisplit")), not(type("bpm")), (trigger("tankof"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") return \"")).output(mark("typeWithNamespace")).output(literal(".\" + ")).output(mark("type")).output(literal(".Split.splitByQn(split).qn();")),
			rule().condition(not(type("bpm")), (trigger("tankof"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") return \"")).output(mark("channel")).output(literal("\";")),
			rule().condition((allTypes("bpm","multisplit")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(", BpmSplit split, BpmSplit... moreSplits) {\n\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n\tfor (BpmSplit c : moreSplits) connector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((type("bpm")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((type("multisplit")), not(type("bpm")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition(not(type("bpm")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((allTypes("bpm","multiSplit")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, BpmSplit split, BpmSplit... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + split.qn(), subscriberId + \"_\" + split.qn(), consumers.get(onEventReceived));\n\tfor (BpmSplit c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + c.qn(), subscriberId + \"_\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, BpmSplit split, BpmSplit... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + split.qn(), consumers.get(onEventReceived));\n\tfor (BpmSplit c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(ProcessStatusConsumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((type("multiSplit")), not(type("bpm")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), subscriberId + \"_\" + split.qn(), consumers.get(onEventReceived));\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), subscriberId + \"_\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), consumers.get(onEventReceived));\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName")).output(literal("Consumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((trigger("quoted"))).output(literal("\"")).output(mark("")).output(literal("\"")),
			rule().condition((trigger("interface"))).output(literal("public interface ")).output(mark("namespaceQn", "firstUpperCase")).output(mark("name", "firstUpperCase")).output(literal("Consumer extends java.util.function.Consumer<")).output(mark("type")).output(literal("> {\n}"))
		);
	}
}