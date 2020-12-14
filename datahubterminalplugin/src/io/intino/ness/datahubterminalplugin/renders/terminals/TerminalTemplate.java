package io.intino.ness.datahubterminalplugin.renders.terminals;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("terminal"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport io.intino.alexandria.Timetag;\nimport io.intino.alexandria.Scale;\nimport io.intino.alexandria.event.Event;\nimport io.intino.alexandria.logger.Logger;\nimport ")).output(mark("package", "validPackage")).output(literal(".events.*;\n\nimport java.util.function.Consumer;\nimport java.util.List;\n\npublic class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\tprivate static final Scale scale = Scale.")).output(mark("scale")).output(literal(";\n\tprivate static final Object monitor = new Object();\n\tprivate final io.intino.alexandria.terminal.Connector connector;\n\tprivate final Realtime realtime;\n\tprivate Lookups lookups;\n\t")).output(mark("bpm", "splits")).output(literal("\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.terminal.Connector connector) {\n\t\tthis.realtime = new Realtime(this.connector = connector);\n\t}\n\n\tpublic Realtime realtime() {\n\t\treturn realtime;\n\t}\n\n\t")).output(expression().output(mark("datalake")).output(literal("\n")).output(literal("public BatchSession batch(java.io.File dataHubStageDirectory, java.io.File temporalStageDirectory) {")).output(literal("\n")).output(literal("\treturn new BatchSession(dataHubStageDirectory, temporalStageDirectory);")).output(literal("\n")).output(literal("}")).output(literal("\n")).output(literal("\n")).output(literal("public BatchSession batch(java.io.File dataHubStageDirectory, java.io.File temporalStageDirectory, Config config) {")).output(literal("\n")).output(literal("\treturn new BatchSession(dataHubStageDirectory, temporalStageDirectory, config);")).output(literal("\n")).output(literal("}")).output(literal("\n")).output(literal(""))).output(literal("\n\t")).output(mark("lookup").multiple("\n")).output(literal("\n\n\tpublic void createDynamicLookups(java.io.File lookupsDirectory) {\n\t\tthis.lookups = new Lookups(lookupsDirectory);\n\t}\n\n\tpublic static class Realtime {\n\t\tpublic static final String[] subscriptionChannels = new String[]{")).output(mark("subscribe", "channel")).output(literal("};\n\t\tprivate final io.intino.alexandria.terminal.Connector connector;\n\t\tprivate final java.util.Map<java.util.function.Consumer<?>, java.util.function.Consumer<io.intino.alexandria.event.Event>> consumers = new java.util.HashMap<>();\n\n\t\tpublic Realtime(io.intino.alexandria.terminal.Connector connector) {\n\t\t\tthis.connector = connector;\n\t\t}\n\n\t\tpublic void feed(Object event, String split) {\n\t\t\t")).output(mark("publish", "if").multiple("\n")).output(literal("\n\t\t}\n\n\t\tpublic void feed(io.intino.alexandria.event.SessionEvent session) {\n\t\t\tconnector.sendEvent(io.intino.alexandria.event.SessionEvent.PATH, session);\n\t\t}\n\n\t\tpublic void subscribe(SessionEventConsumer onEventReceived) {\n\t\t\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new io.intino.alexandria.event.SessionEvent(event.toMessage())));\n\t\t\tconnector.attachListener(io.intino.alexandria.event.SessionEvent.PATH, consumers.get(onEventReceived));\n\t\t}\n\n\t\t")).output(mark("publish").multiple("\n\n")).output(literal("\n\n\t\t")).output(mark("subscribe").multiple("\n\n")).output(literal("\n\t}\n\n\tpublic class BatchSession {\n\t\tprivate final java.io.File dataHubStage;\n\t\tprivate final java.io.File temporalStage;\n\t\tprivate final io.intino.alexandria.ingestion.SessionHandler sessionHandler;\n\t\tprivate final io.intino.alexandria.ingestion.EventSession eventSession;\n\t\tprivate final io.intino.alexandria.ingestion.SetSession setSession;\n\t\tprivate final io.intino.alexandria.ingestion.TransactionSession transactionSession;\n\n\t\tpublic BatchSession(java.io.File dataHubStage, java.io.File temporalStage) {\n\t\t\tthis(dataHubStage, temporalStage, new Config());\n\t\t}\n\n\t\tpublic BatchSession(java.io.File dataHubStage, java.io.File temporalStage, Config config) {\n\t\t\tthis.dataHubStage = dataHubStage;\n\t\t\tthis.temporalStage = temporalStage;\n\t\t\tthis.sessionHandler = new io.intino.alexandria.ingestion.SessionHandler(temporalStage);\n\t\t\tthis.eventSession = sessionHandler.createEventSession();\n\t\t\tthis.setSession = sessionHandler.createSetSession(config.setsBufferSise);\n\t\t\tthis.transactionSession = sessionHandler.createTransactionSession(config.transactionsBufferSise);\n\t\t}\n\n\t\tpublic void feed(Event event, String split) {\n            eventSession.put(tankOf(event, split), Timetag.of(event.ts(), scale), event);\n\t\t}\n\n\t\tpublic void feed(String tank, Timetag timetag, String set, java.util.stream.Stream<Long> ids) {\n            setSession.put(tank, timetag, set, ids);\n\t\t}\n\n\t\t")).output(mark("transaction", "feed").multiple("\n\n")).output(literal("\n\n\t\tpublic void flush() {\n\t\t\ttransactionSession.close();\n\t\t}\n\n\t\tpublic void push() {\n\t\t\teventSession.close();\n\t\t\tsetSession.close();\n\t\t\ttransactionSession.close();\n\t\t\tsessionHandler.pushTo(this.dataHubStage);\n\t\t\t//connector.sendEvent(\"service.ness.push\", new Event(new io.intino.alexandria.message.Message(\"Push\").set(\"stage\", temporalStage.getName())));\n\t\t}\n\n\t\tpublic synchronized void seal() {\n\t\t\tsynchronized(monitor) {\n\t\t\t\tconnector.requestResponse(\"service.ness.seal\", new Event(new io.intino.alexandria.message.Message(\"Seal\").set(\"stage\", temporalStage.getName())).ts(java.time.Instant.now()).toString(), s -> {\n\t\t\t\t\t\tsynchronized(monitor) {\n\t\t\t\t\t\t\tmonitor.notify();\n\t\t\t\t\t\t}\n\t\t\t\t\t}\n\t\t\t\t);\n\t\t\t\ttry {\n\t\t\t\t\tmonitor.wait();\n\t\t\t\t} catch (InterruptedException e) {\n\t\t\t\t\tio.intino.alexandria.logger.Logger.error(e);\n\t\t\t\t}\n\t\t\t}\n        }\n\n        private String tankOf(Event event, String split) {\n        \t")).output(mark("publish", "tankOf").multiple("\n")).output(literal("\n        \treturn event.toMessage().type();\n        }\n\t}\n\n\tpublic static class Config {\n\t\tprivate int transactionsBufferSise = 1_000_000;\n\t\tprivate int setsBufferSise = 1_000_000;\n\n\t\tpublic Config transactionsBufferSise(int transactionsBufferSise) {\n\t\t\tthis.transactionsBufferSise = transactionsBufferSise;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic Config setsBufferSise(int setsBufferSise) {\n\t\t\tthis.setsBufferSise = setsBufferSise;\n\t\t\treturn this;\n\t\t}\n\t}\n\n\tpublic interface SessionEventConsumer extends java.util.function.Consumer<io.intino.alexandria.event.SessionEvent> {\n\t}\n\n\t")).output(mark("event", "interface").multiple("\n\n")).output(literal("\n}")),
			rule().condition((trigger("lookup"))).output(literal("public ")).output(mark("qn")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("() {\n\tif (this.lookups == null) {\n\t\tLogger.warn(\"Lookups are not created\");\n\t\treturn null;\n\t}\n\treturn this.lookups.")).output(mark("name", "firstLowerCase")).output(literal("();\n}")),
			rule().condition((type("bpm")), (trigger("splits"))).output(literal("public enum BpmSplit {\n\t")).output(mark("split", "asEnum").multiple(", ")).output(literal(";\n\n\tpublic abstract String qn();\n\n\tpublic static BpmSplit splitByQn(String qn) {\n\t\treturn java.util.Arrays.stream(values()).filter(c -> c.qn().equals(qn)).findFirst().orElse(null);\n\t}\n}")),
			rule().condition((trigger("asenum"))).output(mark("value", "snakeCaseToCamelCase")).output(literal(" {\n\tpublic String qn() {\n\t\treturn \"")).output(mark("qn")).output(literal("\";\n\t}\n}")),
			rule().condition((allTypes("multisplit","bpm")), (trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") feed((")).output(mark("type")).output(literal(") event, BpmSplit.splitByQn(split));")),
			rule().condition((type("multisplit")), (trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") feed((")).output(mark("type")).output(literal(") event, ")).output(mark("type")).output(literal(".Split.splitByQn(split));")),
			rule().condition((trigger("if"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") feed((")).output(mark("type")).output(literal(") event);")),
			rule().condition((type("multisplit")), not(type("bpm")), (trigger("tankof"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") return \"")).output(mark("typeWithNamespace")).output(literal(".\" + ")).output(mark("type")).output(literal(".Split.splitByQn(split).qn();")),
			rule().condition(not(type("bpm")), (trigger("tankof"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") return \"")).output(mark("channel")).output(literal("\";")),
			rule().condition((allTypes("bpm","multisplit")), (trigger("publish"))).output(literal("public void feed(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(", BpmSplit split, BpmSplit... moreSplits) {\n\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n\tfor (BpmSplit c : moreSplits) connector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((type("bpm")), (trigger("publish"))).output(literal("public void feed(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((type("multisplit")), not(type("bpm")), (trigger("publish"))).output(literal("public void feed(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(", ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.sendEvent(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition(not(type("bpm")), (trigger("publish"))).output(literal("public void feed(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition((allTypes("bpm","multiSplit")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, BpmSplit split, BpmSplit... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + split.qn(), subscriberId, consumers.get(onEventReceived));\n\tfor (BpmSplit c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + c.qn(), subscriberId, consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, BpmSplit split, BpmSplit... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + split.qn(), consumers.get(onEventReceived));\n\tfor (BpmSplit c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeName", "FirstUpperCase")).output(literal(".\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(ProcessStatusConsumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((type("multiSplit")), not(type("bpm")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), subscriberId, consumers.get(onEventReceived));\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), subscriberId, consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, ")).output(mark("type")).output(literal(".Split split, ")).output(mark("type")).output(literal(".Split... moreSplits) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + split.qn(), consumers.get(onEventReceived));\n\tfor (")).output(mark("type")).output(literal(".Split c : moreSplits)\n\t\tconnector.attachListener(\"")).output(mark("typeWithNamespace")).output(literal(".\" + c.qn(), consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName")).output(literal("Consumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.put(onEventReceived, event -> onEventReceived.accept(new ")).output(mark("type")).output(literal("(event)));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", consumers.get(onEventReceived));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("typeName", "FirstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconnector.detachListeners(consumers.get(onEventReceived));\n}")),
			rule().condition((trigger("feed"))).output(literal("public void feed")).output(mark("namespace", "FirstUpperCase")).output(mark("name", "FirstUpperCase")).output(literal("(String tank, Timetag timetag, java.util.function.Consumer<")).output(mark("qn")).output(literal("> transaction) {\n\ttransactionSession.put(tank, timetag, ")).output(mark("qn")).output(literal(".class, transaction);\n}\n\npublic void feed")).output(mark("namespace", "FirstUpperCase")).output(mark("name", "FirstUpperCase")).output(literal("(String tank, Timetag timetag, java.util.stream.Stream<Consumer<")).output(mark("qn")).output(literal(">> stream) {\n\ttransactionSession.put(tank, timetag, ")).output(mark("qn")).output(literal(".class, stream);\n}")),
			rule().condition((trigger("quoted"))).output(literal("\"")).output(mark("")).output(literal("\"")),
			rule().condition((trigger("interface"))).output(literal("public interface ")).output(mark("namespaceQn", "firstUpperCase")).output(mark("name", "firstUpperCase")).output(literal("Consumer extends java.util.function.Consumer<")).output(mark("type")).output(literal("> {\n}"))
		);
	}
}