package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MasterTerminalTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("master","interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.util.Arrays;\nimport java.util.List;\nimport java.util.UUID;\nimport java.util.stream.Stream;\nimport java.util.stream.Collectors;\nimport ")).output(mark("package")).output(literal(".entities.*;\nimport io.intino.ness.master.model.Triplet;\nimport com.hazelcast.core.EntryListener;\n\npublic interface MasterTerminal {\n\n\tstatic MasterTerminal create() {\n\t\treturn create(new MasterTerminal.Config());\n\t}\n\n\tstatic MasterTerminal create(MasterTerminal.Config config) {\n\t\tif(config.type() == Type.FullLoad) return new FullLoadMasterTerminal(config);\n\t\tif(config.type() == Type.LazyLoad) return new LazyLoadMasterTerminal(config);\n\t\tthrow new IllegalArgumentException(\"Unknown MasterTerminal type \" + config.type());\n\t}\n\n\tvoid start();\n\tvoid stop();\n\n\tvoid publish(String senderName, Triplet Triplet);\n\n\tMasterTerminal.Config config();\n\n\tvoid addEntryListener(EntryListener<String, String> listener);\n\n\tio.intino.ness.master.serialization.MasterSerializer serializer();\n\n\t")).output(mark("entity", "getterSignature").multiple("\n\n")).output(literal("\n\n\tenum Type {\n\t\t/**\n\t\t* <p>All records will be loaded into local-memory maps on start.</p>\n\t\t*/\n\t\tFullLoad,\n\n\t\t/**\n\t\t* <p>Records will be loaded from the master backend on demand.</p>\n\t\t*/\n\t\tLazyLoad;\n\n\t\tpublic static Type getDefault() {return FullLoad;}\n\t\tpublic static Type byName(String name) {return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(name)).findFirst().orElse(null);}\n\t}\n\n\tfinal class Config {\n\t\tprivate String instanceName = \"")).output(mark("package")).output(literal(".MasterTerminal-\" + UUID.randomUUID();\n\t\tprivate List<String> addresses = List.of(\"localhost:5701\");\n\t\tprivate Type type = Type.getDefault();\n\t\tprivate boolean readOnly = true;\n\n\t\tpublic Config() {}\n\n\t\tpublic Config(Config other) {\n\t\t\tthis.instanceName = other.instanceName;\n\t\t\tthis.addresses = other.addresses == null ? null : List.copyOf(other.addresses);\n\t\t\tthis.type = other.type;\n\t\t\tthis.readOnly = other.readOnly;\n\t\t}\n\n\t\tpublic String instanceName() {\n\t\t\treturn instanceName;\n\t\t}\n\n\t\tpublic Config instanceName(String instanceName) {\n\t\t\tthis.instanceName = instanceName;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic List<String> addresses() {\n\t\t\treturn addresses;\n\t\t}\n\n\t\tpublic Config addresses(List<String> addresses) {\n\t\t\tthis.addresses = addresses;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic Type type() {\n\t\t\treturn type;\n\t\t}\n\n\t\tpublic Config type(Type type) {\n\t\t\tthis.type = type;\n\t\t\treturn this;\n\t\t}\n\n\t\tpublic boolean readOnly() {\n\t\t\treturn readOnly;\n\t\t}\n\n\t\tpublic Config readOnly(boolean readOnly) {\n\t\t\tthis.readOnly = readOnly;\n\t\t\treturn this;\n\t\t}\n\t}\n}")),
			rule().condition((allTypes("master","cached"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport static io.intino.ness.master.core.Master.*;\nimport static java.util.Objects.requireNonNull;\n\nimport com.hazelcast.client.HazelcastClient;\nimport com.hazelcast.client.config.ClientConfig;\nimport com.hazelcast.client.config.ClientNetworkConfig;\nimport com.hazelcast.core.EntryAdapter;\nimport com.hazelcast.core.EntryEvent;\nimport com.hazelcast.core.EntryListener;\nimport com.hazelcast.core.HazelcastInstance;\nimport com.hazelcast.map.IMap;\nimport io.intino.alexandria.logger.Logger;\nimport io.intino.ness.master.model.Triplet;\nimport static io.intino.ness.master.model.Triplet.TRIPLET_SEPARATOR;\nimport io.intino.ness.master.model.TripletRecord;\nimport io.intino.ness.master.model.Triplet;\n\nimport ")).output(mark("package")).output(literal(".entities.*;\nimport io.intino.ness.master.serialization.MasterSerializer;\nimport io.intino.ness.master.serialization.MasterSerializers;\n\nimport java.util.concurrent.ConcurrentHashMap;\nimport java.util.HashMap;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.stream.Stream;\n\nimport java.util.concurrent.ExecutorService;\nimport java.util.concurrent.Executors;\nimport java.util.concurrent.TimeUnit;\nimport java.util.logging.ConsoleHandler;\nimport java.util.logging.Handler;\nimport java.util.logging.Level;\nimport java.util.logging.LogManager;\n\npublic class FullLoadMasterTerminal implements MasterTerminal {\n\n\t")).output(mark("entity", "map").multiple("\n")).output(literal("\n\n\tprivate final MasterTerminal.Config config;\n\tprivate HazelcastInstance hazelcast;\n\n\tpublic FullLoadMasterTerminal(MasterTerminal.Config config) {\n\t\tthis.config = requireNonNull(config);\n\t}\n\n\t@Override\n\tpublic void start() {\n\t\tconfigureLogger();\n\t\tinitHazelcastClient();\n\t\tloadData();\n\t\tinitListeners();\n\t}\n\n\t@Override\n\tpublic void stop() {\n\t\thazelcast.shutdown();\n\t}\n\n\t@Override\n\tpublic void addEntryListener(EntryListener<String, String> listener) {\n\t\thazelcast.<String, String>getMap(MASTER_MAP_NAME).addEntryListener(listener, true);\n\t}\n\n\t@Override\n\tpublic MasterSerializer serializer() {\n    \tIMap<String, String> metadata = hazelcast.getMap(METADATA_MAP_NAME);\n    \treturn MasterSerializers.get(metadata.get(\"serializer\"));\n    }\n\n\t@Override\n\tpublic MasterTerminal.Config config() {\n\t\treturn new MasterTerminal.Config(config);\n\t}\n\n\t")).output(mark("entity", "getter").multiple("\n\n")).output(literal("\n\n\tpublic void publish(String publisherName, Triplet Triplet) {\n\t\tif(config.readOnly()) throw new UnsupportedOperationException(\"This master client cannot publish because it is configured as read only\");\n\t\tif(publisherName == null) throw new NullPointerException(\"Publisher name cannot be null\");\n\t\tif(Triplet == null) throw new NullPointerException(\"Triplet cannot be null\");\n\t\thazelcast.getTopic(REQUESTS_TOPIC).publish(publisherName + MESSAGE_SEPARATOR + Triplet);\n\t}\n\n\tprivate void add(TripletRecord record) {\n\t\tswitch(record.type()) {\n\t\t\t")).output(mark("entity", "adder").multiple("\n")).output(literal("\n\t\t}\n\t}\n\n\tprivate void remove(String id) {\n\t\tswitch(Triplet.typeOf(id)) {\n\t\t\t")).output(mark("entity", "remover").multiple("\n")).output(literal("\n\t\t}\n\t}\n\n\t")).output(mark("entity", "add").multiple("\n\n")).output(literal("\n\n\t")).output(mark("entity", "remove").multiple("\n\n")).output(literal("\n\n\tprivate void initHazelcastClient() {\n\t\tClientConfig config = new ClientConfig();\n\t\tconfig.setInstanceName(this.config.instanceName());\n\t\tconfig.setNetworkConfig(new ClientNetworkConfig().setAddresses(this.config.addresses()));\n\t\thazelcast = HazelcastClient.newHazelcastClient(config);\n\t}\n\n\tprivate void initListeners() {\n\t\thazelcast.getMap(MASTER_MAP_NAME).addEntryListener(new TripleEntryDispatcher(), true);\n\t}\n\n\tprivate void loadData() {\n\t\tIMap<String, String> master = hazelcast.getMap(MASTER_MAP_NAME);\n\t\tMasterSerializer serializer = serializer();\n\n\t\tLogger.info(\"Loading data from master (serializer=\" + serializer.name() + \")\");\n\t\tlong start = System.currentTimeMillis();\n\n\t\tloadDataMultiThread(master, serializer);\n\n\t\tlong time = System.currentTimeMillis() - start;\n\t\tLogger.info(\"Data from master loaded in \" + time + \" ms => \" + this);\n\t}\n\n\tprivate void loadDataSingleThread(IMap<String, String> master, MasterSerializer serializer) {\n\t\tmaster.forEach((id, serializedRecord) -> add(serializer.deserialize(serializedRecord)));\n\t}\n\n\tprivate void loadDataMultiThread(IMap<String, String> master, MasterSerializer serializer) {\n\t\ttry {\n\t\t\tExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);\n\n\t\t\tmaster.forEach((id, serializedRecord) -> threadPool.submit(() -> add(serializer.deserialize(serializedRecord))));\n\n\t\t\tthreadPool.shutdown();\n\t\t\tthreadPool.awaitTermination(1, TimeUnit.HOURS);\n\t\t} catch (Exception e) {\n\t\t\tthrow new RuntimeException(e);\n\t\t}\n\t}\n\n\tprivate static void configureLogger() {\n\t\tjava.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger(\"\");\n\t\trootLogger.setLevel(Level.WARNING);\n\t\tfor (Handler h : rootLogger.getHandlers()) rootLogger.removeHandler(h);\n\t\tfinal ConsoleHandler handler = new ConsoleHandler();\n\t\thandler.setLevel(Level.WARNING);\n\t\thandler.setFormatter(new io.intino.alexandria.logger.Formatter());\n\t\trootLogger.setUseParentHandlers(false);\n\t\trootLogger.addHandler(handler);\n\t}\n\n\tpublic class TripleEntryDispatcher extends EntryAdapter<String, String> {\n\n\t\t@Override\n\t\tpublic void entryAdded(EntryEvent<String, String> event) {\n\t\t\taddOrUpdateRecord(event.getKey(), event.getValue());\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryUpdated(EntryEvent<String, String> event) {\n\t\t\taddOrUpdateRecord(event.getKey(), event.getValue());\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryRemoved(EntryEvent<String, String> event) {\n\t\t\tremove(event.getKey());\n\t\t}\n\n\t\t@Override\n\t\tpublic void entryEvicted(EntryEvent<String, String> event) {\n\t\t\tremove(event.getKey());\n\t\t}\n\n\t\tprivate void addOrUpdateRecord(String id, String serializedRecord) {\n\t\t\tMasterSerializer serializer = serializer();\n\t\t\tadd(serializer.deserialize(serializedRecord));\n\t\t}\n\t}\n}")),
			rule().condition((allTypes("master","lazy"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport static io.intino.ness.master.core.Master.*;\nimport static java.util.Objects.requireNonNull;\n\nimport com.hazelcast.client.HazelcastClient;\nimport com.hazelcast.client.config.ClientConfig;\nimport com.hazelcast.client.config.ClientNetworkConfig;\nimport com.hazelcast.core.EntryAdapter;\nimport com.hazelcast.core.EntryEvent;\nimport com.hazelcast.core.EntryListener;\nimport com.hazelcast.core.HazelcastInstance;\nimport com.hazelcast.map.IMap;\nimport io.intino.alexandria.logger.Logger;\nimport io.intino.ness.master.model.Triplet;\nimport static io.intino.ness.master.model.Triplet.TRIPLET_SEPARATOR;\nimport io.intino.ness.master.model.TripletRecord;\nimport io.intino.ness.master.model.Triplet;\n\nimport io.intino.ness.master.model.Entity;\nimport ")).output(mark("package")).output(literal(".entities.*;\nimport io.intino.ness.master.serialization.MasterSerializer;\nimport io.intino.ness.master.serialization.MasterSerializers;\n\nimport java.util.List;\nimport java.util.Map;\nimport java.util.stream.Stream;\nimport java.util.function.BiFunction;\n\nimport java.util.logging.ConsoleHandler;\nimport java.util.logging.Handler;\nimport java.util.logging.Level;\nimport java.util.logging.LogManager;\n\npublic class LazyLoadMasterTerminal implements MasterTerminal {\n\n\tprivate final MasterTerminal.Config config;\n\tprivate HazelcastInstance hazelcast;\n\tprivate IMap<String, String> masterMap;\n\tprivate MasterSerializer serializer;\n\n\tpublic LazyLoadMasterTerminal(MasterTerminal.Config config) {\n\t\tthis.config = requireNonNull(config);\n\t}\n\n\t@Override\n\tpublic void start() {\n\t\tconfigureLogger();\n\t\tinitHazelcastClient();\n\t}\n\n\t@Override\n\tpublic void stop() {\n\t\thazelcast.shutdown();\n\t}\n\n\t@Override\n\tpublic void addEntryListener(EntryListener<String, String> listener) {\n\t\thazelcast.<String, String>getMap(MASTER_MAP_NAME).addEntryListener(listener, true);\n\t}\n\n\t@Override\n    public MasterSerializer serializer() {\n    \treturn serializer;\n    }\n\n\t@Override\n\tpublic MasterTerminal.Config config() {\n\t\treturn new MasterTerminal.Config(config);\n\t}\n\n\t")).output(mark("entity", "getter").multiple("\n\n")).output(literal("\n\n\tprivate TripletRecord getRecord(String id) {\n    \tString serializedRecord = masterMap.get(id);\n    \tif(serializedRecord == null) return null;\n    \treturn serializer.deserialize(serializedRecord);\n    }\n\n   \t\tprivate <T extends Entity> T entity(BiFunction<String, MasterTerminal, T> constructor, String id, TripletRecord record) {\n   \t\t\tT entity = constructor.apply(id, this);\n   \t\t\trecord.triplets().forEach(entity::add);\n   \t\t\treturn entity;\n   \t\t}\n\n\tpublic void publish(String publisherName, Triplet Triplet) {\n\t\tif(config.readOnly()) throw new UnsupportedOperationException(\"This master client cannot publish because it is configured as read only\");\n\t\tif(publisherName == null) throw new NullPointerException(\"Publisher name cannot be null\");\n\t\tif(Triplet == null) throw new NullPointerException(\"Triplet cannot be null\");\n\t\thazelcast.getTopic(REQUESTS_TOPIC).publish(publisherName + MESSAGE_SEPARATOR + Triplet);\n\t}\n\n\tprivate void initHazelcastClient() {\n\t\tClientConfig config = new ClientConfig();\n    \tconfig.setInstanceName(this.config.instanceName());\n    \tconfig.setNetworkConfig(new ClientNetworkConfig().setAddresses(this.config.addresses()));\n\n    \thazelcast = HazelcastClient.newHazelcastClient(config);\n\n    \tmasterMap = hazelcast.getMap(MASTER_MAP_NAME);\n    \tIMap<String, String> metadata = hazelcast.getMap(METADATA_MAP_NAME);\n    \tserializer = MasterSerializers.get(metadata.get(\"serializer\"));\n\t}\n\n\tprivate static void configureLogger() {\n\t\tjava.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger(\"\");\n\t\trootLogger.setLevel(Level.WARNING);\n\t\tfor (Handler h : rootLogger.getHandlers()) rootLogger.removeHandler(h);\n\t\tfinal ConsoleHandler handler = new ConsoleHandler();\n\t\thandler.setLevel(Level.WARNING);\n\t\thandler.setFormatter(new io.intino.alexandria.logger.Formatter());\n\t\trootLogger.setUseParentHandlers(false);\n\t\trootLogger.addHandler(handler);\n\t}\n}")),
			rule().condition(not(type("abstract")), (trigger("remover"))).output(literal("case \"")).output(mark("name", "lowerCase")).output(literal("\": removeFrom")).output(mark("name", "FirstUpperCase")).output(literal("(id); break;")),
			rule().condition(not(type("abstract")), (trigger("adder"))).output(literal("case \"")).output(mark("name", "lowerCase")).output(literal("\": addTo")).output(mark("name", "FirstUpperCase")).output(literal("(record); break;")),
			rule().condition(not(type("abstract")), (trigger("map"))).output(literal("private final Map<String, ")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "FirstLowerCase")).output(literal("Map = new ConcurrentHashMap<>();")),
			rule().condition(not(type("abstract")), (trigger("add"))).output(literal("private void addTo")).output(mark("name", "FirstUpperCase")).output(literal("(TripletRecord record) {\n\t")).output(mark("name", "FirstUpperCase")).output(literal(" entity = new ")).output(mark("name", "FirstUpperCase")).output(literal("(record.id(), this);\n\trecord.triplets().forEach(entity::add);\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.put(record.id(), entity);\n}")),
			rule().condition(not(type("abstract")), (trigger("remove"))).output(literal("private void removeFrom")).output(mark("name", "FirstUpperCase")).output(literal("(String id) {\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.remove(id);\n}")),
			rule().condition((type("subclass")), (trigger("getbyid"))).output(literal("case \"")).output(mark("name", "lowerCase")).output(literal("\": return ")).output(mark("name", "firstLowerCase")).output(literal("(id);")),
			rule().condition((type("subclass")), (trigger("getallstream"))).output(mark("name", "Plural", "firstLowerCase")).output(literal("()")),
			rule().condition((type("abstract")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\tswitch(Triplet.typeOf(id)) {\n\t\t")).output(mark("subclass", "getById").multiple("\n")).output(literal("\n\t}\n\treturn null;\n}\n\n@Override\npublic Stream<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn Stream.of(\n\t\t")).output(mark("subclass", "getAllStream").multiple(",\n")).output(literal("\n\t).flatMap(java.util.function.Function.identity());\n}")),
			rule().condition(not(type("abstract")), (type("cached")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Map.get(id);\n}\n\n@Override\npublic Stream<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Map.values().stream();\n}")),
			rule().condition(not(type("abstract")), (type("lazy")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\tTripletRecord record = getRecord(id);\n\treturn record != null ? entity(")).output(mark("name", "FirstUpperCase")).output(literal("::new, id, record) : null;\n}\n\n@Override\npublic Stream<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn masterMap.entrySet().stream()\n\t\t\t.filter(e -> e.getKey().endsWith(\":")).output(mark("name", "firstLowerCase")).output(literal("\"))\n\t\t\t.map(e -> entity(")).output(mark("name", "FirstUpperCase")).output(literal("::new, e.getKey(), serializer.deserialize(e.getValue())));\n}")),
			rule().condition((trigger("gettersignature"))).output(literal("public ")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id);\npublic Stream<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("();\ndefault List<")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("List() {return ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("().collect(Collectors.toList());}"))
		);
	}
}