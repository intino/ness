package io.intino.ness.datahubterminalplugin.terminal;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class TerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("terminal"))).output(literal("package ")).output(mark("package", "validPackage")).output(literal(";\n\nimport io.intino.alexandria.Scale;\nimport io.intino.alexandria.Timetag;\nimport io.intino.alexandria.event.Event;\nimport io.intino.alexandria.event.message.MessageEvent;\nimport io.intino.alexandria.logger.Logger;\nimport org.apache.activemq.command.ActiveMQTextMessage;\n\nimport javax.jms.JMSException;\nimport javax.jms.Message;\nimport javax.jms.TextMessage;\nimport java.io.File;\nimport java.time.Instant;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.Collections;\nimport java.util.concurrent.TimeUnit;\nimport java.util.stream.Stream;\nimport java.util.stream.Collectors;\n\nimport java.lang.reflect.Field;\nimport java.lang.reflect.Method;\n\npublic class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\tpublic static String[] subscriptionChannels = new String[]{")).output(mark("subscribe", "channel")).output(literal("};\n\tprivate final io.intino.alexandria.terminal.Connector connector;\n\tprivate final java.util.Map<java.util.function.BiConsumer<?, String>, List<java.util.function.Consumer<io.intino.alexandria.event.Event>>> consumers = new java.util.HashMap<>();\n\t")).output(expression().output(mark("datamart", "declaration").multiple("\n"))).output(literal("\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.terminal.Connector connector) {\n\t\t")).output(expression().output(literal("this(connector, ")).output(mark("datamart", "retryConfigDefault")).output(literal(");")).next(expression().output(literal("this.connector = connector;")))).output(literal("\n\t}\n\n\t")).output(expression().output(mark("datamart", "constructor"))).output(literal("\n\n\t")).output(expression().output(mark("datamart", "getter").multiple("\n"))).output(literal("\n\n\tpublic void initDatamarts() {\n\t\t")).output(expression().output(mark("datamart", "init").multiple("\n"))).output(literal("\n\t}\n\n\tpublic void publish(Event event) {\n\t\tswitch(event.type()) {\n\t\t\t")).output(mark("publish", "publishSwitchCase").multiple("\n")).output(literal("\n\t\t}\n\t}\n\n\tpublic void publish(Event first, Event ...others) {\n\t\tpublish(Stream.concat(Stream.of(first), java.util.Arrays.stream(others)));\n\t}\n\n\tpublic void publish(Event[] events) {\n\t\tpublish(java.util.Arrays.stream(events));\n\t}\n\n\tpublic void publish(java.util.Collection<Event> events) {\n\t\tpublish(events.stream());\n\t}\n\n\tpublic void publish(Stream<Event> events) {\n\t\tevents.filter(e -> channelOf(e.type()) != null)\n\t\t\t.collect(Collectors.groupingBy(Event::type))\n\t\t\t.forEach((type, eventList) -> connector.sendEvents(channelOf(type), eventList));\n\t}\n\n\tpublic io.intino.alexandria.datalake.Datalake datalake() {\n\t\ttry {\n\t\t\tMessage message = connector.requestResponse(\"service.ness.datalake.messagestore\", request(\"Datalake\"), 5, TimeUnit.SECONDS);\n\t\t\tif(message == null) return null;\n\n\t\t\tString path = ((TextMessage)message).getText();\n\t\t\tif(path == null) return null;\n\n\t\t\treturn new File(path).exists()\n\t\t\t\t\t? new io.intino.alexandria.datalake.file.FileDatalake(new File(path))\n\t\t\t\t\t: new io.intino.alexandria.terminal.remotedatalake.RemoteDatalake((io.intino.alexandria.terminal.JmsConnector) connector);\n\n\t\t} catch (JMSException e) {\n\t\t\tLogger.error(e);\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tpublic BatchSession batch(java.io.File temporalStageDirectory) {\n\t\treturn new BatchSession(temporalStageDirectory);\n\t}\n\n\tpublic BatchSession batch(java.io.File temporalStageDirectory, Config config) {\n\t\treturn new BatchSession(temporalStageDirectory, config);\n\t}\n\n\t")).output(mark("publish").multiple("\n\n")).output(literal("\n\n\t")).output(mark("subscribe").multiple("\n\n")).output(literal("\n\n\tpublic synchronized void requestSeal() {\n\t\tconnector.requestResponse(\"service.ness.seal\", request(\"Seal\"), 30, TimeUnit.MINUTES);\n\t}\n\n\tpublic synchronized Instant requestLastSeal() {\n\t\tMessage message = connector.requestResponse(\"service.ness.seal.last\", request(\"LastSeal\"), 10, TimeUnit.MINUTES);\n\t\tif(message == null) return Instant.now();\n\t\ttry {\n\t\t\treturn Instant.parse(((TextMessage)message).getText());\n\t\t} catch (Exception e) {\n\t\t\tLogger.error(e);\n\t\t\treturn Instant.now();\n\t\t}\n\t}\n\n\tprivate javax.jms.Message request(String type) {\n\t\treturn request(type, Collections.emptyMap());\n\t}\n\n\tprivate javax.jms.Message request(String type, Map<String, String> attributes) {\n\t\ttry {\n\t\t\tActiveMQTextMessage m = new ActiveMQTextMessage();\n\t\t\tio.intino.alexandria.message.Message message = new io.intino.alexandria.message.Message(type);\n\t\t\tattributes.forEach(message::set);\n\t\t\tm.setText(message.toString());\n\t\t\treturn m;\n\t\t} catch(Exception e) {\n\t\t\tthrow new RuntimeException(e);\n\t\t}\n\t}\n\n\t")).output(expression().output(mark("datamart", "addDatamartSubscribers").multiple("\n\n"))).output(literal("\n\n\tprivate String channelOf(String type) {\n\t\tswitch(type) {\n\t\t\t")).output(expression().output(mark("publish", "channelOfSwitchCase").multiple("\n"))).output(literal("\n\t\t}\n\t\treturn null;\n\t}\n\n\tpublic class BatchSession {\n\t\tprivate final java.io.File temporalStage;\n\t\tprivate final io.intino.alexandria.ingestion.SessionHandler sessionHandler;\n\t\tprivate final io.intino.alexandria.ingestion.EventSession session;\n\t\tprivate final Scale scale;\n\n\t\tpublic BatchSession(java.io.File temporalStage) {\n\t\t\tthis(temporalStage, new Config());\n\t\t}\n\n\t\tpublic BatchSession(java.io.File temporalStage, Config config) {\n\t\t\tthis.temporalStage = temporalStage;\n\t\t\tthis.scale = config.scale;\n\t\t\tthis.sessionHandler = new io.intino.alexandria.ingestion.SessionHandler(temporalStage);\n\t\t\tthis.session = sessionHandler.createEventSession(config.eventsBufferSize);\n\t\t}\n\n\t\tpublic void feed(Event event) throws java.io.IOException {\n\t\t\tsession.put(tankOf(event), event.ss(), Timetag.of(event.ts(), this.scale), event.format(), event);\n\t\t}\n\n\t\tpublic void feed(Event event, Scale scale) throws java.io.IOException {\n\t\t\tsession.put(tankOf(event), event.ss(), Timetag.of(event.ts(), scale), event.format(), event);\n\t\t}\n\n\t\tpublic void flush() {\n\t\t\tsession.flush();\n\t\t}\n\n\t\tpublic void push(File dataHubStage) {\n\t\t\tsession.close();\n\t\t\tsessionHandler.pushTo(dataHubStage);\n\t\t\t//connector.sendEvent(\"service.ness.push\", new Event(new io.intino.alexandria.message.Message(\"Push\").set(\"stage\", temporalStage.getName())));\n\t\t}\n\n\t\tpublic void push(String host, String user, String dataHubStageAbsolutePath) {\n\t\t\tsession.close();\n\t\t\ttry {\n\t\t\t\tList<File> files = allFilesIn(temporalStage.toPath(), path -> path.getName().endsWith(io.intino.alexandria.Session.SessionExtension)).collect(Collectors.toList());\n\t\t\t\tupload(files, host, user, dataHubStageAbsolutePath);\n\t\t\t\ttemporalStage.renameTo(new File(temporalStage.getParentFile(), temporalStage.getName() + \".treated\"));\n\t\t\t} catch(Exception e) {\n\t\t\t\tLogger.error(e);\n\t\t\t}\n\t\t}\n\n\t\tprivate static Stream<File> allFilesIn(java.nio.file.Path path, java.util.function.Predicate<File> filter) throws Exception {\n\t\t\tStream.Builder<File> streamBuilder = Stream.builder();\n\t\t\ttry (Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(path)) {\n\t\t\t\tpaths.filter(p -> java.nio.file.Files.isRegularFile(p) && filter.test(p.toFile())).forEach(p -> streamBuilder.add(p.toFile()));\n\t\t\t}\n\t\t\treturn streamBuilder.build();\n\t\t}\n\n\t\tpublic synchronized void seal() {\n\t\t\tconnector.requestResponse(\"service.ness.seal\", request(\"Seal\", Map.of(\"stage\", temporalStage.getName())));\n\t\t}\n\n\t\tprivate void upload(List<File> sessions, String host, String user, String dataHubStageAbsolutePath) {\n\t\t\ttry {\n\t\t\t\tString connectionChain = user + \"@\" + host + \":\" + dataHubStageAbsolutePath;\n\t\t\t\tLogger.info(\"Uploading sessions to \" + connectionChain + \"...\");\n\t\t\t\tfor (File s : sessions) {\n\t\t\t\t\tProcess process = new ProcessBuilder(\"scp\", s.getAbsolutePath(), connectionChain)\n\t\t\t\t\t\t\t.inheritIO()\n\t\t\t\t\t\t\t.start();\n\t\t\t\t\tprocess.waitFor(1, java.util.concurrent.TimeUnit.HOURS);\n\t\t\t\t}\n\t\t\t\tLogger.info(\"sessions uploaded\");\n\t\t\t} catch (java.io.IOException | InterruptedException ignored) {\n\t\t\t}\n\n\t\t}\n\n\t\tprivate String tankOf(Event event) {\n\t\t\t")).output(mark("publish", "tankOf").multiple("\n")).output(literal("\n\t\t\treturn event.type();\n\t\t}\n\t}\n\n\tpublic static class Config {\n\t\tprivate int eventsBufferSize = 1_000_000;\n\t\tprivate int setsBufferSize = 1_000_000;\n\t\tprivate Scale scale = Scale.")).output(mark("scale")).output(literal(";\n\n\t\tpublic Config scale(Scale scale) {\n\t\t\tthis.scale = scale;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic Config eventsBufferSize(int eventsBufferSize) {\n\t\t\tthis.eventsBufferSize = eventsBufferSize;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic Config setsBufferSize(int setsBufferSize) {\n\t\t\tthis.setsBufferSize = setsBufferSize;\n\t\t\treturn this;\n\t\t}\n\t}\n\n\t")).output(mark("message", "interface").multiple("\n\n")).output(literal("\n\t")).output(mark("measurement", "interface").multiple("\n\n")).output(literal("\n\t")).output(mark("processstatus", "interface").multiple("\n\n")).output(literal("\n\n\t")).output(expression().output(mark("datamart", "retryConfigClass"))).output(literal("\n}")),
			rule().condition((trigger("retryconfigclass"))).output(literal("public static class DatamartsRetryConfig {\n\tpublic final long initialTimeoutAmount;\n\tpublic final java.util.concurrent.TimeUnit timeoutUnit;\n\tpublic final float timeoutMultiplier;\n\tpublic final int maxAttempts;\n\n\tpublic DatamartsRetryConfig() {\n\t\tthis(1, java.util.concurrent.TimeUnit.MINUTES, 2.0f, 5);\n\t}\n\n\tpublic DatamartsRetryConfig(long initialTimeoutAmount, java.util.concurrent.TimeUnit timeoutUnit, float timeoutMultiplier, int maxAttempts) {\n\t\tthis.initialTimeoutAmount = initialTimeoutAmount;\n\t\tthis.timeoutUnit = timeoutUnit;\n\t\tthis.timeoutMultiplier = timeoutMultiplier;\n\t\tthis.maxAttempts = maxAttempts;\n\t}\n}")),
			rule().condition((trigger("constructor"))).output(literal("public ")).output(mark("terminal", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(io.intino.alexandria.terminal.Connector connector, DatamartsRetryConfig datamartsRetryConfig) {\n\tthis.connector = connector;\n\tthis.")).output(mark("name", "firstLowerCase")).output(literal("Datamart = new ")).output(mark("package")).output(literal(".")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl(connector, datamartsRetryConfig);\n}")),
			rule().condition((trigger("init"))).output(mark("name", "firstLowerCase")).output(literal("Datamart.init();\naddDatamartSubscribers(this.")).output(mark("name", "firstLowerCase")).output(literal("Datamart);")),
			rule().condition((trigger("retryconfigassign"))).output(literal("this.datamartsRetryConfig = datamartsRetryConfig;")),
			rule().condition((trigger("retryconfigdefault"))).output(literal("new DatamartsRetryConfig()")),
			rule().condition((type("datamart")), (trigger("declaration"))).output(literal("private final ")).output(mark("package")).output(literal(".")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl ")).output(mark("name", "firstLowerCase")).output(literal("Datamart;")),
			rule().condition((type("datamart")), (trigger("instantiate"))),
			rule().condition((type("datamart")), (trigger("getter"))).output(literal("public ")).output(mark("package")).output(literal(".")).output(mark("name", "FirstUpperCase")).output(literal("Datamart ")).output(mark("name", "firstLowerCase")).output(literal("Datamart() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Datamart;\n}")),
			rule().condition((type("datamart")), (trigger("adddatamartsubscribers"))).output(literal("private void addDatamartSubscribers(")).output(mark("package")).output(literal(".")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl datamart) {\n\t")).output(expression().output(mark("devent", "addSubscribe").multiple("\n"))).output(literal("\n}")),
			rule().condition((type("devent")), (trigger("addsubscribe"))).output(literal("subscribe((")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer) (event, topic) -> datamart.mount(event), connector.clientId() + \"_")).output(mark("datamart")).output(literal("_")).output(mark("message")).output(literal("\");")),
			rule().condition((trigger("publishswitchcase"))).output(literal("case \"")).output(mark("typename", "FirstUpperCase")).output(literal("\": publish((event instanceof ")).output(mark("type")).output(literal(" ? ((")).output(mark("type")).output(literal(") event) : new ")).output(mark("type")).output(literal("(((io.intino.alexandria.event.message.MessageEvent) event).toMessage()))); break;")),
			rule().condition((trigger("channelofswitchcase"))).output(literal("case \"")).output(mark("typename", "FirstUpperCase")).output(literal("\": return \"")).output(mark("channel")).output(literal("\";")),
			rule().condition(not(type("bpm")), (trigger("tankof"))).output(literal("if (event instanceof ")).output(mark("type")).output(literal(") return \"")).output(mark("channel")).output(literal("\";")),
			rule().condition((type("bpm")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("typeName", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("typeName", "firstLowerCase")).output(literal(");\n}")),
			rule().condition(not(type("bpm")), (type("measurement")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("message", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("message", "firstLowerCase")).output(literal(".toMessageEvent());\n}")),
			rule().condition(not(type("bpm")), (trigger("publish"))).output(literal("public void publish(")).output(mark("type")).output(literal(" ")).output(mark("message", "firstLowerCase")).output(literal(") {\n\tconnector.sendEvent(\"")).output(mark("channel")).output(literal("\", ")).output(mark("message", "firstLowerCase")).output(literal(");\n}\n\npublic void publish(")).output(mark("type")).output(literal(" first, ")).output(mark("type")).output(literal(" ...others) {\n\t")).output(mark("type")).output(literal("[] array = new ")).output(mark("type")).output(literal("[1 + others.length];\n\tarray[0] = first;\n\tjava.lang.System.arraycopy(others, 0, array, 1, others.length);\n\tpublish(array);\n}\n\npublic void publish(")).output(mark("type")).output(literal("[] events) {\n\tconnector.sendEvents(\"")).output(mark("channel")).output(literal("\", java.util.Arrays.asList(events));\n}")),
			rule().condition((type("measurement")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("(((MessageEvent) event).toMessage()), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, java.util.function.Predicate<Instant> filter) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("(((MessageEvent) event).toMessage()), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0), filter);\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("(((MessageEvent) event).toMessage()), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", consumers.get(onEventReceived).get(0));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.get(onEventReceived).forEach(connector::detachListeners);\n}")),
			rule().condition((type("message")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((MessageEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, java.util.function.Predicate<Instant> filter) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((MessageEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0), filter);\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((MessageEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", consumers.get(onEventReceived).get(0));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.get(onEventReceived).forEach(connector::detachListeners);\n}")),
			rule().condition((type("resource")), (trigger("subscribe"))).output(literal("public void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((ResourceEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0));\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived, String subscriberId, java.util.function.Predicate<Instant> filter) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((ResourceEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", subscriberId, consumers.get(onEventReceived).get(0), filter);\n}\n\npublic void subscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.put(onEventReceived, List.of(event -> { try { onEventReceived.accept(new ")).output(mark("type")).output(literal("((ResourceEvent) event), \"")).output(mark("channel")).output(literal("\");} catch(Throwable e) { Logger.error(e); }}));\n\tconnector.attachListener(\"")).output(mark("channel")).output(literal("\", consumers.get(onEventReceived).get(0));\n}\n\npublic void unsubscribe(")).output(mark("namespaceQn", "firstUpperCase")).output(mark("message", "firstUpperCase")).output(literal("Consumer onEventReceived) {\n\tconsumers.get(onEventReceived).forEach(connector::detachListeners);\n}")),
			rule().condition((trigger("quoted"))).output(literal("\"")).output(mark("")).output(literal("\"")),
			rule().condition((trigger("interface"))).output(literal("public interface ")).output(mark("namespaceQn", "firstUpperCase")).output(mark("name", "firstUpperCase")).output(literal("Consumer extends java.util.function.BiConsumer<")).output(mark("type")).output(literal(", String> {\n}"))
		);
	}
}