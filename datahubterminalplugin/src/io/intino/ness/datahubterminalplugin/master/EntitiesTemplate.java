package io.intino.ness.datahubterminalplugin.master;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class EntitiesTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("master","view"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.util.Arrays;\nimport java.util.List;\nimport java.util.UUID;\nimport java.util.stream.Stream;\nimport java.util.stream.Collectors;\nimport ")).output(mark("package")).output(literal(".entities.*;\n\npublic interface EntitiesView {\n\t")).output(mark("entity", "getterSignature").multiple("\n\n")).output(literal("\n}")),
			rule().condition((allTypes("master","interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport io.intino.ness.master.messages.Response;\nimport io.intino.ness.master.messages.listeners.EntityListener;\nimport io.intino.ness.master.messages.listeners.ErrorListener;\nimport io.intino.ness.master.model.Entity;\nimport io.intino.ness.master.model.TripletRecord;\n\nimport java.util.concurrent.Future;\nimport java.util.concurrent.atomic.AtomicReference;\n\nimport ")).output(mark("package")).output(literal(".entities.*;\n\npublic interface Entities extends EntitiesView {\n\n\tSingleton Instance = new Singleton();\n\n\tstatic Entities get() {\n\t\tEntities entities = Instance.get();\n\t\tif(entities == null) throw new IllegalStateException(\"Entities is not initialized!\");\n\t\treturn entities;\n\t}\n\n\tvoid enable(String entityId);\n\tvoid disable(String entityId);\n\tvoid publish(Entity entity);\n\n\tEntitiesView disabled();\n\n\tio.intino.ness.master.serialization.MasterSerializer serializer();\n\n\tvoid addErrorListener(ErrorListener listener);\n\n\t<T extends Entity> void addEntityListener(String type, EntityListener<T> listener);\n\n\t")).output(mark("entity", "enable").multiple("\n\n")).output(literal("\n\t")).output(mark("entity", "disable").multiple("\n\n")).output(literal("\n\n\t")).output(mark("entity", "entityListener").multiple("\n\n")).output(literal("\n\n\t@SuppressWarnings(\"unchecked\")\n\tdefault <T extends Entity> T asEntity(TripletRecord record) {\n\t\tif(record == null) return null;\n\t\tswitch(record.type()) {\n\t\t\t")).output(mark("entity", "asEntitySwitchCase").multiple("\n")).output(literal("\n\t\t\tdefault: throw new IllegalArgumentException(\"Unknown entity type \" + record.type());\n\t\t}\n\t}\n\n\t/**expectedType must include the : prefix*/\n\tdefault String normalizeId(String id, String expectedType) {\n\t\treturn id.endsWith(expectedType) ? id : id + expectedType;\n\t}\n\n\tfinal class Singleton {\n\t\tprivate final AtomicReference<Entities> instance = new AtomicReference<>();\n\t\tprivate Singleton() {}\n\t\tprivate Entities get() {\n\t\t\treturn instance.get();\n\t\t}\n\t\tprivate void set(Entities entities) {\n\t\t\tinstance.compareAndSet(null, java.util.Objects.requireNonNull(entities));\n\t\t}\n\t}\n}")),
			rule().condition((allTypes("master","cached"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport io.intino.alexandria.logger.Logger;\nimport io.intino.alexandria.terminal.Connector;\nimport io.intino.ness.master.core.MasterInitializationException;\nimport io.intino.ness.master.messages.DownloadMasterMessage;\nimport io.intino.ness.master.messages.MasterMessageSerializer;\nimport io.intino.ness.master.messages.Response;\nimport io.intino.ness.master.messages.UpdateMasterMessage;\nimport io.intino.ness.master.messages.listeners.EntityListener;\nimport io.intino.ness.master.messages.listeners.EntityListener.Event;\nimport io.intino.ness.master.messages.listeners.ErrorListener;\nimport io.intino.ness.master.model.Entity;\nimport io.intino.ness.master.model.Triplet;\nimport io.intino.ness.master.model.TripletRecord;\nimport io.intino.ness.master.serialization.MasterMapSerializer;\nimport io.intino.ness.master.serialization.MasterSerializer;\nimport io.intino.ness.master.serialization.MasterSerializers;\nimport org.apache.activemq.command.ActiveMQTextMessage;\n\nimport javax.jms.Message;\nimport java.time.Instant;\nimport java.util.ArrayList;\nimport java.util.Collections;\nimport java.util.List;\nimport java.util.Set;\nimport java.util.Map;\nimport java.util.concurrent.*;\nimport java.util.concurrent.atomic.AtomicBoolean;\nimport java.util.stream.Stream;\n\nimport static io.intino.ness.master.messages.DownloadMasterMessage.PROPERTY_ENTITY_SERIALIZER;\nimport static io.intino.ness.master.messages.DownloadMasterMessage.PROPERTY_ERROR;\nimport static java.util.Objects.requireNonNull;\n\nimport ")).output(mark("package", "validPackage")).output(literal(".master.Entities;\nimport ")).output(mark("package", "validPackage")).output(literal(".master.EntitiesView;\nimport ")).output(mark("package")).output(literal(".master.entities.*;\n\n@SuppressWarnings({\"rawtypes\", \"unchecked\"})\npublic class CachedEntities implements Entities {\n\n\tprivate static final String ENTITIES_TOPIC = \"entities\";\n\tprivate static final String ENTITY_STORE_SERVICE_PATH = \"service.ness.datalake.entitystore\";\n\n\tprivate static final Set<String> PublishEntities = Set.of(")).output(expression().output(mark("entity", "publishItem").multiple(", "))).output(literal(");\n\tprivate static final Set<String> SubscribeEntities = Set.of(")).output(expression().output(mark("entity", "subscribeItem").multiple(", "))).output(literal(");\n\n\t")).output(mark("entity", "map").multiple("\n")).output(literal("\n\n\tprivate final Connector connector;\n\tprivate final AtomicBoolean initialized = new AtomicBoolean(false);\n\tprivate final Map<String, List<EntityListener>> entityListeners = new ConcurrentHashMap<>();\n\tprivate final List<ErrorListener> errorListeners = Collections.synchronizedList(new ArrayList<>());\n\tprivate final Map<String, CompletableFuture> futures = new ConcurrentHashMap<>();\n\tprivate MasterSerializer serializer;\n\tprivate final DisabledEntitiesView disabledView = new DisabledEntitiesView();\n\tprivate EntitiesMessageConsumer entityConsumer;\n\n\tCachedEntities(Connector connector) {\n\t\tthis.connector = requireNonNull(connector);\n\t}\n\n\tsynchronized void init() {\n\t\ttry {\n\t\t\tif(!initialized.compareAndSet(false, true)) return;\n\t\t\tloadData();\n\t\t\tinitListeners();\n\t\t\tLogger.info(\"Entities terminal \" + connector.clientId() + \" initialized successfully. (\" + getClass().getSimpleName() + \")\");\n\t\t} catch(Exception e) {\n\t\t\tthrow new MasterInitializationException(\"Entities failed to start: \" + e.getMessage(), e);\n\t\t}\n\t}\n\n\t@Override\n\tpublic void addErrorListener(ErrorListener listener) {\n\t\tif(listener == null) throw new NullPointerException(\"ErrorListener cannot be null\");\n\t\terrorListeners.add(listener);\n\t}\n\n\t@Override\n\tpublic <T extends Entity> void addEntityListener(String type, EntityListener<T> listener) {\n\t\tif(type == null) throw new NullPointerException(\"Type cannot be null\");\n\t\tif(listener == null) throw new NullPointerException(\"EntryListener cannot be null\");\n\t\tentityListeners.computeIfAbsent(type, k -> new ArrayList<>(1)).add(listener);\n\t}\n\n\t@Override\n\tpublic MasterSerializer serializer() {\n    \treturn serializer;\n    }\n\n\t")).output(mark("entity", "getter").multiple("\n\n")).output(literal("\n\n\t@Override\n\tpublic void enable(String id) {\n        if(id == null || id.isBlank()) throw new NullPointerException(\"Entity id cannot be null nor blank\");\n        if(publishIsDisabledFor(Triplet.typeOf(id))) throw new UnsupportedOperationException(\"This terminal is not subscribed to \" + capitalize(Triplet.typeOf(id)));\n        UpdateMasterMessage message = createMessage(UpdateMasterMessage.Intent.Enable, id);\n\t\tthis.entityConsumer.accept(message);\n        publishMessage(message);\n\t}\n\n  \t@Override\n  \tpublic void disable(String id) {\n        if(id == null || id.isBlank()) throw new NullPointerException(\"Entity id cannot be null nor blank\");\n        if(publishIsDisabledFor(Triplet.typeOf(id))) throw new UnsupportedOperationException(\"This terminal is not subscribed to \" + capitalize(Triplet.typeOf(id)));\n        UpdateMasterMessage message = createMessage(UpdateMasterMessage.Intent.Disable, id);\n        this.entityConsumer.accept(message);\n        publishMessage(message);\n  \t}\n\n\t@Override\n\tpublic void publish(Entity entity) {\n\t\tif(entity == null) throw new NullPointerException(\"Entity cannot be null\");\n\t\tif(publishIsDisabledFor(entity.id().type())) throw new UnsupportedOperationException(\"This terminal is not subscribed to \" + capitalize(entity.id().type()));\n\t\tUpdateMasterMessage message = createMessage(UpdateMasterMessage.Intent.Publish, serializer().serialize(entity.asTripletRecord()));\n\t\tthis.entityConsumer.accept(message);\n\t\tpublishMessage(message);\n\t}\n\n\tprivate static boolean publishIsDisabledFor(String type) {\n\t\treturn !PublishEntities.contains(capitalize(type));\n\t}\n\n\tprivate static String capitalize(String s) {\n\t\treturn Character.toUpperCase(s.charAt(0)) + s.substring(1);\n\t}\n\n\tprivate void waitFor(Future<?> future) {\n\t\ttry {\n\t\t\tfuture.get();\n\t\t} catch(Exception e) {\n\t\t\tthrow new RuntimeException(e);\n\t\t}\n\t}\n\n\tprivate void publishMessage(UpdateMasterMessage message) {\n\t\tconnector.sendTopicMessage(ENTITIES_TOPIC, MasterMessageSerializer.serialize(message));\n\t}\n\n\t@Override\n\tpublic EntitiesView disabled() {\n\t\treturn disabledView;\n\t}\n\n\tprivate UpdateMasterMessage createMessage(UpdateMasterMessage.Intent intent, String value) {\n\t\treturn new UpdateMasterMessage(connector.clientId(), intent, value);\n\t}\n\n\tprivate boolean isEnabled(TripletRecord record) {\n\t\tString enabledValue = record.getValue(\"enabled\");\n        return enabledValue == null || \"true\".equalsIgnoreCase(enabledValue);\n\t}\n\n\tprivate boolean isDisabled(TripletRecord record) {\n\t\treturn !isEnabled(record);\n\t}\n\n\tprotected Event.Type addEntityInternal(TripletRecord record) {\n\t\tswitch(record.type()) {\n\t\t\t")).output(mark("entity", "addEntitySwitchCase").multiple("\n")).output(literal("\n\t\t}\n\t\treturn Event.Type.None;\n\t}\n\n\tprotected Event.Type enableEntityInternal(String id) {\n\t\tswitch(Triplet.typeOf(id)) {\n\t\t\t")).output(mark("entity", "enableEntitySwitchCase").multiple("\n")).output(literal("\n\t\t}\n\t\treturn Event.Type.None;\n\t}\n\n\tprotected Event.Type disableEntityInternal(String id) {\n\t\tswitch(Triplet.typeOf(id)) {\n\t\t\t")).output(mark("entity", "disableEntitySwitchCase").multiple("\n")).output(literal("\n\t\t}\n\t\treturn Event.Type.None;\n\t}\n\n\t")).output(mark("entity", "addEntityInternal").multiple("\n\n")).output(literal("\n\n\t")).output(mark("entity", "enableEntityInternal").multiple("\n\n")).output(literal("\n\n\t")).output(mark("entity", "disableEntityInternal").multiple("\n\n")).output(literal("\n\n\tprotected void initListeners() {\n\t\tthis.entityConsumer = new EntitiesMessageConsumer();\n\t\tconnector.attachListener(ENTITIES_TOPIC, connector.clientId() + \"-\" + ENTITIES_TOPIC, entityConsumer);\n\t}\n\n\tprivate void loadData() {\n\t\tLogger.debug(\"Loading data from master...\");\n\t\tlong start = System.currentTimeMillis();\n\t\tinitializeEntityMaps(downloadMasterData(DownloadMasterMessage.EntityFilter.AllEntities));\n\t\tlong time = System.currentTimeMillis() - start;\n\t\tLogger.debug(\"Data from master loaded in \" + time + \" ms\");\n\t}\n\n\tprivate Map<String, String> downloadMasterData(DownloadMasterMessage.EntityFilter filter) {\n\t\ttry {\n\t\t\tMessage message = connector.requestResponse(ENTITY_STORE_SERVICE_PATH, downloadMessage(filter));\n\t\t\treturn handleResponse(message);\n\t\t} catch (Exception e) {\n\t\t\tthrow new MasterInitializationException(\"Could not load data from master: \" + e.getMessage(), e);\n\t\t}\n\t}\n\n\tprivate Message downloadMessage(DownloadMasterMessage.EntityFilter filter) {\n\t\ttry {\n\t\t\tDownloadMasterMessage m = new DownloadMasterMessage(SubscribeEntities, filter);\n\t\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\t\tmessage.setText(MasterMessageSerializer.serialize(m));\n\t\t\tmessage.compress();\n\t\t\treturn message;\n\t\t} catch (Exception e) {\n\t\t\tthrow new RuntimeException(e);\n\t\t}\n\t}\n\n\tprivate Map<String, String> handleResponse(javax.jms.Message message) throws Exception {\n\t\tif(message.getBooleanProperty(PROPERTY_ERROR)) {\n\t\t\tthrow new RuntimeException(((ActiveMQTextMessage) message).getText());\n\t\t}\n\t\tthis.serializer = MasterSerializers.get(message.getStringProperty(PROPERTY_ENTITY_SERIALIZER));\n\t\tString serializedMap = ((ActiveMQTextMessage) message).getText();\n\t\treturn MasterMapSerializer.deserialize(serializedMap);\n\t}\n\n\tprivate void initializeEntityMaps(Map<String, String> entities) {\n\t\tif(availableThreads() >= 4)\n\t\t\tloadDataMultiThread(entities, serializer);\n\t\telse\n\t\t\tloadDataSingleThread(entities, serializer);\n\t}\n\n\tprivate void loadDataSingleThread(Map<String, String> entities, MasterSerializer serializer) {\n\t\tentities.forEach((id, serializedRecord) -> addEntityInternal(serializer.deserialize(serializedRecord)));\n\t}\n\n\tprivate void loadDataMultiThread(Map<String, String> entities, MasterSerializer serializer) {\n\t\ttry {\n\t\t\tExecutorService threadPool = Executors.newFixedThreadPool(availableThreads());\n\t\t\tentities.forEach((id, serializedRecord) -> threadPool.submit(() -> addEntityInternal(serializer.deserialize(serializedRecord))));\n\t\t\tthreadPool.shutdown();\n\t\t\tthreadPool.awaitTermination(1, TimeUnit.HOURS);\n\t\t} catch (Exception e) {\n\t\t\tthrow new RuntimeException(e);\n\t\t}\n\t}\n\n\tprivate static int availableThreads() {\n\t\treturn Runtime.getRuntime().availableProcessors() - 1;\n\t}\n\n\tprivate class EntitiesMessageConsumer implements Connector.MessageConsumer {\n\n\t\t@Override\n\t\tpublic void accept(String rawMessage, String callback) {\n\t\t\taccept((UpdateMasterMessage) MasterMessageSerializer.deserialize(rawMessage));\n\t\t}\n\n\t\tpublic void accept(UpdateMasterMessage message) {\n\t\t\ttry {\n\t\t\t\tTripletRecord record = serializer().tryDeserialize(message.value()).orElse(null);\n\t\t\t\tEvent.Type type = process(message, record, message.value());\n\t\t\t\tMasterEntityEvent<?> event = new MasterEntityEvent<>();\n\t\t\t\tevent.type = type;\n\t\t\t\tevent.ts = message.ts();\n\t\t\t\tevent.messageId = message.id();\n\t\t\t\tevent.clientName = message.clientName();\n\t\t\t\tevent.entity = asEntity(record);\n\t\t\t\tevent.entityId = new Entity.Id(record != null ? record.id() : message.value());\n\t\t\t\tevent.value = message.value();\n\t\t\t\tnotifyEntityListeners(event);\n\t\t\t} catch (Throwable e) {\n\t\t\t\thandleError(message, e);\n\t\t\t}\n\t\t}\n\n\t\tprivate void handleError(UpdateMasterMessage message, Throwable e) {\n\t\t\tnotifyErrorListeners(new ErrorListener.Error() {\n\t\t\t\t@Override\n\t\t\t\tpublic Instant ts() {\n\t\t\t\t\treturn message.ts();\n\t\t\t\t}\n\n\t\t\t\t@Override\n\t\t\t\tpublic Throwable cause() {\n\t\t\t\t\treturn e;\n\t\t\t\t}\n\n\t\t\t\t@Override\n\t\t\t\tpublic String clientName() {\n\t\t\t\t\treturn message.clientName();\n\t\t\t\t}\n\n\t\t\t\t@Override\n\t\t\t\tpublic String messageId() {\n\t\t\t\t\treturn message.id();\n\t\t\t\t}\n\t\t\t});\n\t\t}\n\n\t\tprotected Event.Type process(UpdateMasterMessage message, TripletRecord record, String value) {\n\t\t\tswitch(message.intent()) {\n\t\t\t\tcase Publish: return addEntityInternal(record);\n\t\t\t\tcase Enable: return enableEntityInternal(value);\n\t\t\t\tcase Disable: return disableEntityInternal(record.id());\n\t\t\t}\n\t\t\tthrow new IllegalArgumentException(\"Unknown intent \" + message.intent());\n\t\t}\n\n\t\t@SuppressWarnings(\"all\")\n\t\tprotected void notifyEntityListeners(Event<?> event) {\n\t\t\tCompletableFuture<Response<?>> future = futures.remove(event.messageId());\n\t\t\tif(future != null) future.complete(Response.ofSuccessful(event));\n\t\t\tList<EntityListener> listeners = entityListeners.get(event.entityId().type());\n\t\t\tif(listeners != null) listeners.forEach(listener -> listener.notify(event));\n\t\t}\n\n\t\t@SuppressWarnings(\"all\")\n\t\tprotected void notifyErrorListeners(ErrorListener.Error error) {\n\t\t\tCompletableFuture<Response<?>> future = futures.remove(error.messageId());\n\t\t\tif(future != null) future.complete(Response.ofFailure(error));\n\t\t\terrorListeners.forEach(listener -> listener.notify(error));\n\t\t}\n\t}\n\n\tpublic static class MasterEntityEvent<T extends Entity> implements Event<T> {\n\n\t\tprivate String clientName;\n\t\tprivate Type type;\n\t\tprivate Entity.Id entityId;\n\t\tprivate T entity;\n\t\tprivate String value;\n\t\tprivate Instant ts;\n\t\tprivate String messageId;\n\n\t\tprivate MasterEntityEvent() {}\n\n\t\t@Override\n\t\tpublic String clientName() {\n\t\t\treturn clientName;\n\t\t}\n\n\t\t@Override\n\t\tpublic Type type() {\n\t\t\treturn type;\n\t\t}\n\n\t\t@Override\n\t\tpublic Entity.Id entityId() {\n\t\t\treturn entityId;\n\t\t}\n\n\t\t@Override\n\t\tpublic T entity() {\n\t\t\treturn entity;\n\t\t}\n\n\t\t@Override\n\t\tpublic String value() {\n\t\t\treturn value;\n\t\t}\n\n\t\t@Override\n\t\tpublic Instant ts() {\n\t\t\treturn ts;\n\t\t}\n\n\t\t@Override\n\t\tpublic String messageId() {\n\t\t\treturn messageId;\n\t\t}\n\t}\n\n\tprivate class DisabledEntitiesView implements EntitiesView {\n\n\t\t")).output(mark("entity", "map").multiple("\n")).output(literal("\n\n\t\t")).output(mark("entity", "getter").multiple("\n\n")).output(literal("\n\t}\n}")),
			rule().condition((type("publish")), (trigger("publishitem"))).output(literal("\"")).output(mark("name", "FirstUpperCase")).output(literal("\"")),
			rule().condition((type("subscribe")), (trigger("subscribeitem"))).output(literal("\"")).output(mark("name", "FirstUpperCase")).output(literal("\"")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("addentityswitchcase"))).output(literal("case \"")).output(mark("name", "firstLowerCase")).output(literal("\": return addTo")).output(mark("name", "FirstUpperCase")).output(literal("Internal(record);")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("enableentityswitchcase"))).output(literal("case \"")).output(mark("name", "firstLowerCase")).output(literal("\": return enable")).output(mark("name", "FirstUpperCase")).output(literal("Internal(id);")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("disableentityswitchcase"))).output(literal("case \"")).output(mark("name", "firstLowerCase")).output(literal("\": return disable")).output(mark("name", "FirstUpperCase")).output(literal("Internal(id);")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("map"))).output(literal("private final Map<String, ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "FirstLowerCase")).output(literal("Map = new ConcurrentHashMap<>();")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("addentityinternal"))).output(literal("private Event.Type addTo")).output(mark("name", "FirstUpperCase")).output(literal("Internal(TripletRecord record) {\n\t")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" entity = new ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("(record.id());\n\trecord.triplets().forEach(entity::add);\n\tif (!entity.enabled()) {\n    \tdisabledView.")).output(mark("name", "firstLowerCase")).output(literal("Map.put(record.id(), entity);\n    \treturn Event.Type.None;\n    }\n\t")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" old = ")).output(mark("name", "firstLowerCase")).output(literal("Map.get(record.id());\n\tif (entity.deepEquals(old)) return Event.Type.None;\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.put(record.id(), entity);\n\treturn old == null ? Event.Type.Create : Event.Type.Update;\n}")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("enableentityinternal"))).output(literal("private Event.Type enable")).output(mark("name", "FirstUpperCase")).output(literal("Internal(String id) {\n\tif(")).output(mark("name", "firstLowerCase")).output(literal("Map.containsKey(id)) return Event.Type.None;\n\t")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" entity = disabledView.")).output(mark("name", "firstLowerCase")).output(literal("Map.remove(id);\n\tif (entity == null) return Event.Type.None;\n\tentity.add(new Triplet(id, \"enabled\", \"true\"));\n\t")).output(mark("name", "firstLowerCase")).output(literal("Map.put(id, entity);\n\treturn Event.Type.Enable;\n}")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("disableentityinternal"))).output(literal("private Event.Type disable")).output(mark("name", "FirstUpperCase")).output(literal("Internal(String id) {\n\tif(!")).output(mark("name", "firstLowerCase")).output(literal("Map.containsKey(id)) return Event.Type.None;\n\n\t")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" entity = ")).output(mark("name", "firstLowerCase")).output(literal("Map.remove(id);\n\tif(entity == null) return Event.Type.None;\n\n\tentity.add(new Triplet(id, \"enabled\", \"false\"));\n\tdisabledView.")).output(mark("name", "firstLowerCase")).output(literal("Map.put(id, entity);\n\n\treturn Event.Type.Disable;\n}")),
			rule().condition((type("subclass")), (trigger("getbyid"))).output(literal("case \"")).output(mark("name", "lowerCase")).output(literal("\": return ")).output(mark("name", "firstLowerCase")).output(literal("(id);")),
			rule().condition((type("subclass")), (trigger("getallstream"))).output(mark("name", "Plural", "firstLowerCase")).output(literal("()")),
			rule().condition((type("abstract")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\tif(id == null) return null;\n\tswitch(Triplet.typeOf(id)) {\n\t\t")).output(mark("subclass", "getById").multiple("\n")).output(literal("\n\t}\n\treturn null;\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn Stream.of(\n\t\t")).output(mark("subclass", "getAllStream").multiple(",\n")).output(literal("\n\t).flatMap(java.util.function.Function.identity());\n}")),
			rule().condition(not(type("abstract")), (type("subscribe")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\tif(id == null) return null;\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Map.get(normalizeId(id, \":")).output(mark("name", "firstLowerCase")).output(literal("\"));\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn ")).output(mark("name", "firstLowerCase")).output(literal("Map.values().stream();\n}")),
			rule().condition(not(type("abstract")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\tthrow new UnsupportedOperationException(\"This terminal is not subscribed to ")).output(mark("name", "FirstUpperCase")).output(literal("\");\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\tthrow new UnsupportedOperationException(\"This terminal is not subscribed to ")).output(mark("name", "FirstUpperCase")).output(literal("\");\n}")),
			rule().condition((trigger("gettersignature"))).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id);\nStream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("();\ndefault List<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("List() {return ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("().collect(Collectors.toList());}")),
			rule().condition((trigger("entitylistener"))).output(literal("default void add")).output(mark("name", "FirstUpperCase")).output(literal("EntityListener(EntityListener<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> listener) {\n\taddEntityListener(\"")).output(mark("name", "firstLowerCase")).output(literal("\", listener);\n}")),
			rule().condition((type("entity")), not(type("abstract")), (trigger("asentityswitchcase"))).output(literal("case \"")).output(mark("name", "firstLowerCase")).output(literal("\": return (T) new ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("(record);")),
			rule().condition((type("entity")), not(type("abstract")), (trigger("enable"))).output(literal("default void enable")).output(mark("name", "FirstUpperCase")).output(literal("(String id) {\n\tif(id == null) throw new NullPointerException(\"Id cannot be null\");\n\tenable(normalizeId(id, \"")).output(mark("name", "firstLowerCase")).output(literal("\"));\n}")),
			rule().condition((type("entity")), not(type("abstract")), (trigger("disable"))).output(literal("default void disable")).output(mark("name", "FirstUpperCase")).output(literal("(String id) {\n\tif(id == null) throw new NullPointerException(\"Id cannot be null\");\n\tdisable(normalizeId(id, \"")).output(mark("name", "firstLowerCase")).output(literal("\"));\n}"))
		);
	}
}