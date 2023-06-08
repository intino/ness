package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DatamartTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("datamart", "interface"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.util.List;\nimport java.util.Map;\nimport java.util.concurrent.ConcurrentHashMap;\nimport java.util.stream.Stream;\nimport java.util.stream.Collectors;\nimport java.time.Instant;\nimport java.util.Optional;\n\nimport io.intino.ness.master.Datamart;\nimport io.intino.ness.master.model.Entity;\nimport io.intino.ness.master.reflection.*;\n\npublic interface ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart extends Datamart {\n\n\tDatamartDefinition definition = new ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart.DatamartDefinitionInternal();\n\n\tList<String> listSnapshots();\n\t")).output(mark("name", "FirstUpperCase")).output(literal("Datamart snapshot(String timetag);\n\n\t")).output(expression().output(mark("entity", "getterSignature").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("hasTimelines")).output(literal("Stream<TimelineNode> timelines(String id);"))).output(literal("\n\n\t")).output(expression().output(mark("timeline", "getterSignature").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("hasReels")).output(literal("Stream<ReelNode> reels(String id);"))).output(literal("\n\n\t")).output(expression().output(mark("reel", "getterSignature").multiple("\n\n"))).output(literal("\n\n\tclass Entities {\n\n\t\tprivate final ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart datamart;\n\t\tprivate final Map<EntityDefinition, Map<String, ")).output(mark("name", "FirstUpperCase")).output(literal("Entity>> entitiesByType;\n\n\t\tpublic Entities(")).output(mark("name", "FirstUpperCase")).output(literal("Datamart datamart) {\n\t\t\tthis.datamart = datamart;\n\t\t\tthis.entitiesByType = new ConcurrentHashMap<>();\n\t\t\tdatamart.getDefinition().entities().stream().filter(e -> !e.isAbstract()).forEach(entity -> entitiesByType.put(entity, new ConcurrentHashMap<>()));\n\t\t}\n\n\t\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart datamart() {\n\t\t\treturn datamart;\n\t\t}\n\n    \tpublic int size() {\n    \t\treturn entitiesByType.values().stream().mapToInt(Map::size).sum();\n    \t}\n\n    \tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("Entity get(String id) {\n    \t\treturn mapOf(id).map(map -> map.get(id)).orElse(null);\n    \t}\n\n    \tpublic <T extends ")).output(mark("name", "FirstUpperCase")).output(literal("Entity> T getDescendant(EntityDefinition definition, String id) {\n    \t\tT entity = get(definition, id);\n    \t\treturn entity != null ? entity: definition.descendants().stream()\n    \t\t\t.filter(descendant -> !descendant.isAbstract())\n    \t\t\t.map(descendant -> this.<T>get(descendant, id))\n    \t\t\t.filter(java.util.Objects::nonNull).findFirst().orElse(null);\n    \t}\n\n\t\t@SuppressWarnings(\"unchecked\")\n    \tpublic <T extends ")).output(mark("name", "FirstUpperCase")).output(literal("Entity> T get(EntityDefinition type, String id) {\n    \t\treturn entitiesByType.containsKey(type) ? (T) entitiesByType.get(type).get(id) : null;\n        }\n\n    \tpublic void add(")).output(mark("name", "FirstUpperCase")).output(literal("Entity entity) {\n    \t\tentitiesByType.get(entity.getDefinition()).put(entity.id(), entity);\n    \t}\n\n    \tpublic void remove(String id) {\n    \t\tmapOf(id).ifPresent(map -> map.remove(id));\n    \t}\n\n    \tpublic Stream<")).output(mark("name", "FirstUpperCase")).output(literal("Entity> stream() {\n    \t\treturn entitiesByType.values().stream().flatMap(map -> map.values().stream());\n    \t}\n\n\t\t@SuppressWarnings(\"unchecked\")\n    \tpublic <T extends ")).output(mark("name", "FirstUpperCase")).output(literal("Entity> Stream<T> stream(EntityDefinition type) {\n    \t\treturn (Stream<T>) (entitiesByType.containsKey(type) ? entitiesByType.get(type).values().stream() : Stream.empty());\n    \t}\n\n    \tpublic Stream<Entity> streamGeneric() {\n        \treturn entitiesByType.values().stream().flatMap(map -> map.values().stream());\n        }\n\n    \tprivate java.util.Optional<Map<String, ")).output(mark("name", "FirstUpperCase")).output(literal("Entity>> mapOf(String id) {\n    \t\treturn entitiesByType.values().stream().filter(map -> map.containsKey(id)).findFirst();\n    \t}\n    }\n\n    interface ChronosNode {\n    \t/**<p>Returns the id of the chronos object</p>*/\n    \tString id();\n    \t/**<p>Returns the type of the chronos object, as defined in the model</p>*/\n    \tString type();\n    \t/**Clears this node's internal cache, if any, and notifies the datamart to unload this node from memory.*/\n    \tvoid dispose();\n    }\n\n    interface TimelineNode extends ChronosNode {\n\n\t\tio.intino.sumus.chronos.TimelineFile.TimeModel timeModel();\n\t\tio.intino.sumus.chronos.TimelineFile.SensorModel sensorModel();\n\t\tInstant first();\n\t\tInstant last();\n    \tio.intino.sumus.chronos.Timeline get();\n\n    \tvoid setEventListener(EventListener listener);\n\n \t\tinterface EventListener {\n    \t\tvoid onEventReceived(TimelineNode node, io.intino.alexandria.event.Event event);\n    \t}\n    }\n\n    interface ReelNode extends ChronosNode {\n\n    \tio.intino.sumus.chronos.Reel.State stateOf(String signal);\n\t\tdefault List<io.intino.sumus.chronos.Reel.State> stateOf(List<String> signals) {return signals.isEmpty() ? java.util.Collections.emptyList() : stateOf(signals.stream());}\n\t\tList<io.intino.sumus.chronos.Reel.State> stateOf(Stream<String> signals);\n    \tio.intino.sumus.chronos.Reel get(io.intino.sumus.chronos.Period period);\n    \tio.intino.sumus.chronos.Reel get(java.time.Instant from, java.time.Instant to, io.intino.sumus.chronos.Period period);\n    \tvoid setEventListener(EventListener listener);\n\n \t\tinterface EventListener {\n    \t\tvoid onEventReceived(ReelNode node, io.intino.alexandria.event.Event event);\n    \t}\n    }\n\n\tfinal class DatamartDefinitionInternal implements DatamartDefinition {\n\t\tprivate DatamartDefinition definition;\n\t\tprivate DatamartDefinitionInternal() {}\n\t\t@Override\n\t\tpublic String name() {return definition().name();}\n\t\t@Override\n\t\tpublic Datamart.Scale scale() {return definition().scale();}\n\t\t@Override\n\t\tpublic Query<EntityDefinition> entities() {return definition().entities();}\n\t\t@Override\n\t\tpublic Query<StructDefinition> structs() {return definition().structs();}\n\t\t@Override\n\t\tpublic Optional<EntityDefinition> entity(String fullName) {\n        \treturn definition().entity(fullName);\n        }\n        @Override\n        public Optional<StructDefinition> struct(String fullName) {\n        \treturn definition().struct(fullName);\n        }\n\t\tprivate DatamartDefinition definition() {\n\t\t\tif(definition == null) throw new IllegalStateException(\"")).output(mark("name", "FirstUpperCase")).output(literal("Datamart is not initialized\");\n\t\t\treturn definition;\n\t\t}\n\t}\n}")),
				rule().condition((allTypes("datamart", "message", "impl"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport io.intino.alexandria.Timetag;\nimport io.intino.alexandria.event.Event;\nimport io.intino.alexandria.logger.Logger;\nimport io.intino.alexandria.terminal.Connector;\nimport io.intino.ness.master.reflection.*;\nimport io.intino.ness.master.model.Entity;\n\nimport org.apache.activemq.command.ActiveMQTextMessage;\n\nimport java.io.File;\nimport java.util.*;\nimport java.util.concurrent.atomic.AtomicBoolean;\nimport java.util.stream.Stream;\nimport java.util.stream.Collectors;\nimport java.time.LocalDate;\nimport java.time.LocalDateTime;\nimport java.time.Instant;\n\nimport ")).output(mark("ontologypackage")).output(literal(".*;\n\nimport static java.util.Objects.requireNonNull;\n\npublic class ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl implements ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart {\n\n\tprivate static final String DATAHUB_MESSAGE_TOPIC = \"service.ness.datamarts\";\n\tprivate static final ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl.")).output(mark("name", "FirstUpperCase")).output(literal("DatamartDefinition definition = new ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl.")).output(mark("name", "FirstUpperCase")).output(literal("DatamartDefinition();\n\n\t")).output(expression().output(literal("private static final Set<String> TIMELINE_EVENTS = Set.of(")).output(mark("timelineEvents")).output(literal(");"))).output(literal("\n\t")).output(expression().output(literal("private static final Set<String> REEL_EVENTS = Set.of(")).output(mark("reelEvents")).output(literal(");"))).output(literal("\n\n\tprivate final Connector connector;\n\tprivate final ")).output(mark("terminal")).output(literal(".DatamartsRetryConfig retryConfig;\n\tprivate final AtomicBoolean initialized = new AtomicBoolean(false);\n\tprivate final List<EntityListener> entityListeners = new ArrayList<>();\n\tprivate final Map<String, List<MasterMounter>> mounters = new HashMap<>();\n\tprivate final ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart.Entities entities;\n\t")).output(expression().output(mark("hasTimelines")).output(literal("private final Map<String, TimelineNodeImpl> timelines = new java.util.concurrent.ConcurrentHashMap<>();"))).output(literal("\n\t")).output(expression().output(mark("hasReels")).output(literal("private final Map<String, ReelNodeImpl> reels = new java.util.concurrent.ConcurrentHashMap<>();"))).output(literal("\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl(Connector connector, ")).output(mark("terminal")).output(literal(".DatamartsRetryConfig retryConfig) {\n\t\tthis.connector = requireNonNull(connector);\n\t\tthis.retryConfig = requireNonNull(retryConfig);\n\t\tthis.entities = new ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart.Entities(this);\n\t\tinitMounters();\n\t}\n\n\tpublic synchronized ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl init(String datamartSourceSelector) {\n\t\ttry {\n\t\t\tif (!initialized.compareAndSet(false, true)) return this;\n\t\t\tdownloadDatamartFromDatahub(datamartSourceSelector);\n\t\t\tLogger.info(\"")).output(mark("name", "FirstUpperCase")).output(literal("Datamart (\" + (snapshotTimetag().isEmpty() ? \"\" : \"snapshot \" + snapshotTimetag() + \", \")  + connector.clientId() + \") initialized successfully.\");\n\t\t} catch(Exception e) {\n\t\t\tthrow new ExceptionInInitializerError(\"")).output(mark("name", "FirstUpperCase")).output(literal("Datamart failed to start because a \" + e.getClass().getName() + \" occurred: \" + e.getMessage());\n\t\t}\n\t\treturn this;\n\t}\n\n\t@Override\n\tpublic int size() {\n\t\treturn entities.size();\n\t}\n\n\t@Override\n\t@SuppressWarnings(\"unchecked\")\n\tpublic <T extends Entity> T get(String id) {\n\t\treturn (T) entities.get(id);\n\t}\n\n\t@Override\n\tpublic Stream<Entity> entities() {\n\t\treturn entities.streamGeneric();\n\t}\n\n\t@Override\n\tpublic void addEntityListener(EntityListener listener) {\n\t\tif(listener == null) throw new NullPointerException(\"EntityListener cannot be null\");\n\t\tentityListeners.add(listener);\n\t}\n\n\t@Override\n\tpublic DatamartDefinition getDefinition() {\n\t\treturn definition;\n\t}\n\n\t@Override\n    public List<String> listSnapshots() {\n    \ttry {\n    \t\tjavax.jms.Message message = requestResponseFromDatahub(\"listSnapshots\", listSnapshotsRequest());\n    \t\treturn handleListSnapshotsResponse(message);\n    \t} catch (Exception e) {\n    \t\tLogger.error(\"Could not download list of available snapshots: \" + e.getMessage(), e);\n    \t\treturn java.util.Collections.emptyList();\n    \t}\n    }\n\n    private javax.jms.Message listSnapshotsRequest() throws Exception {\n    \tActiveMQTextMessage message = new ActiveMQTextMessage();\n    \tmessage.setText(\"datamart=\" + name() + \";operation=snapshots\");\n    \treturn message;\n    }\n\n    private List<String> handleListSnapshotsResponse(javax.jms.Message message) throws Exception {\n    \treturn java.util.Arrays.stream(((javax.jms.TextMessage) message).getText().split(\",\")).collect(Collectors.toList());\n    }\n\n    @Override\n    public synchronized ")).output(mark("name", "FirstUpperCase")).output(literal("Datamart snapshot(String timetag) {\n    \tif(timetag == null) return this;\n    \treturn new ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartImpl(connector, retryConfig) {\n    \t\t@Override\n    \t\tprotected String snapshotTimetag() {\n    \t\t\treturn timetag;\n    \t\t}\n    \t\t@Override\n    \t\tpublic synchronized MasterDatamart snapshot(String timetag) {\n    \t\t\tthrow new java.lang.UnsupportedOperationException(\"Cannot request snapshots to snapshot instances of a datamart\");\n    \t\t}\n    \t}.init();\n    }\n\n\t")).output(expression().output(mark("entity", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("hasTimelines", "timelinesByIdMethod"))).output(literal("\n\t")).output(expression().output(mark("timeline", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("hasReels", "reelsByIdMethod"))).output(literal("\n\t")).output(expression().output(mark("reel", "getter").multiple("\n\n"))).output(literal("\n\n\tprivate void downloadDatamartFromDatahub(String datamartSourceSelector) {\n\t\tif (connector instanceof io.intino.alexandria.terminal.StubConnector) return;\n\t\tLogger.debug(\"Downloading datamart from datahub...\");\n\t\tlong start = java.lang.System.currentTimeMillis();\n\t\tint[] eventCount = new int[1];\n\t\tloadEntitiesFromEvents(downloadEntities(eventCount), eventCount);\n\t\tlong time = java.lang.System.currentTimeMillis() - start;\n\t\tLogger.debug(\"Datamart downloaded from datahub after \" + time + \" ms\");\n\t}\n\n\tprivate Stream<Event> downloadEntities(int[] eventCount, String datamartSourceSelector) {\n\t\ttry {\n\t\t\tjavax.jms.Message message = requestResponseFromDatahub(\"downloadEvents\", downloadEntitiesRequest(datamartSourceSelector));\n\t\t\treturn handleDownloadResponse(message, eventCount);\n\t\t} catch (NullPointerException e) {\n\t\t\tthrow new NullPointerException(\"Could not download datamart: no response from datahub.\");\n\t\t} catch (Exception e) {\n\t\t\tthrow new RuntimeException(\"Could not download datamart: \" + e.getMessage());\n\t\t}\n\t}\n\n\tprivate javax.jms.Message downloadEntitiesRequest(String datamartSourceSelector) throws Exception {\n\t\tActiveMQTextMessage message = new ActiveMQTextMessage();\n\t\tmessage.setText(\"datamart=\" + name() +\n\t\t\t\";operation=entities\" +\n\t\t\t(snapshotTimetag().isEmpty() ? \"\" : \";timetag=\" + snapshotTimetag())\n\t\t\t(datamartSourceSelector != null ? \"\" : \";ss=\" + datamartSourceSelector)\n\t\t);\n\t\treturn message;\n\t}\n\n\tprotected String snapshotTimetag() {\n\t\treturn \"\";\n\t}\n\n\tprivate Stream<Event> handleDownloadResponse(javax.jms.Message message, int[] eventCount) throws Exception {\n\t\tjavax.jms.BytesMessage m = (javax.jms.BytesMessage) message;\n\t\teventCount[0] = m.getIntProperty(\"size\");\n\t\tint size = m.getIntProperty(\"size\");\n\t\tbyte[] bytes = new byte[size];\n\t\tm.readBytes(bytes, size);\n\t\treturn io.intino.alexandria.zim.ZimStream.of(new java.io.ByteArrayInputStream(bytes)).map(io.intino.alexandria.event.message.MessageEvent::new);\n\t}\n\n\tprivate void loadEntitiesFromEvents(Stream<Event> events, int[] eventCount) {\n\t\tif(availableThreads() >= 4 && eventCount[0] > 1000)\n\t\t\tevents.parallel().forEach(this::mount);\n\t\telse\n\t\t\tevents.forEach(this::mount);\n\t}\n\n\tpublic void mount(Event event) {\n\t\tif(event == null) return;\n\t\tmountEntities(event);\n\t\t")).output(expression().output(mark("hasTimelines")).output(literal("mountTimelines(event);"))).output(literal("\n\t\t")).output(expression().output(mark("hasReels")).output(literal("mountReels(event);"))).output(literal("\n\t}\n\n\t")).output(expression().output(mark("hasReels")).output(literal("private void mountReels(Event event) {")).output(literal("\n")).output(literal("\tif(reels.isEmpty() || !REEL_EVENTS.contains(event.type())) return;")).output(literal("\n")).output(literal("\treels.values().forEach(reel -> reel.notifyEvent(event));")).output(literal("\n")).output(literal("}"))).output(literal("\n\n\t")).output(expression().output(mark("hasTimelines")).output(literal("private void mountTimelines(Event event) {")).output(literal("\n")).output(literal("\tif(timelines.isEmpty() || !TIMELINE_EVENTS.contains(event.type())) return;")).output(literal("\n")).output(literal("\ttimelines.values().forEach(timeline -> timeline.notifyEvent(event));")).output(literal("\n")).output(literal("}"))).output(literal("\n\n\tprivate void mountEntities(Event event) {\n\t\ttry {\n\t\t\tjava.util.Optional.ofNullable(this.mounters.get(event.type())).ifPresent(mounters -> mounters.forEach(mounter -> mounter.mount(event)));\n\t\t} catch(Exception e) {\n\t\t\tLogger.error(\"Failed to mount event of type \" + event.type() + \": \" + e.getMessage(), e);\n\t\t}\n\t}\n\n\tprivate void initMounters() {\n\t\t")).output(expression().output(mark("entity", "registerMounter").multiple("\n"))).output(literal("\n\t}\n\n\tprivate static int availableThreads() {\n\t\treturn Runtime.getRuntime().availableProcessors();\n\t}\n\n\tprivate javax.jms.Message requestResponseFromDatahub(String requestName, javax.jms.Message request) throws Exception {\n    \tlong timeout = retryConfig.initialTimeoutAmount;\n    \tfor(int i = 0;i < retryConfig.maxAttempts;i++) {\n    \t\tjavax.jms.Message message = connector.requestResponse(DATAHUB_MESSAGE_TOPIC, request, timeout, retryConfig.timeoutUnit);\n    \t\tif(message != null) return message;\n    \t\tif(i < retryConfig.maxAttempts - 1) Logger.warn(\"(\"+(i+1)+\") Datahub did not respond after \" + timeout + \" \" + retryConfig.timeoutUnit + \" to the request '\" + requestName + \"'. Trying again...\");\n    \t\ttimeout *= retryConfig.timeoutMultiplier;\n    \t}\n    \tthrow new RuntimeException(\"Datahub did not respond to the request '\" + requestName + \"' after \" + retryConfig.maxAttempts);\n    }\n\n\t")).output(expression().output(mark("timelineNode", "nodeImpl"))).output(literal("\n\n\t")).output(expression().output(mark("reelNode", "nodeImpl"))).output(literal("\n\n\tprivate static Set<String> sourcesOfTimeline(String type) {\n    \treturn switch(type) {\n    \t\t")).output(expression().output(mark("timeline", "sourcesSwitchCase").multiple("\n"))).output(literal("\n    \t\tdefault -> java.util.Collections.emptySet();\n    \t};\n    }\n\n\tprivate static Set<String> sourcesOfReel(String type) {\n    \treturn switch(type) {\n    \t\t")).output(expression().output(mark("reel", "sourcesSwitchCase").multiple("\n"))).output(literal("\n    \t\tdefault -> java.util.Collections.emptySet();\n    \t};\n    }\n\n\t// WARNING: extremely compacted and ugly code ahead... continue at your own discretion.\n\tpublic static final class ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartDefinition implements DatamartDefinition {\n\n\t\t@Override\n\t\tpublic String name() {\n\t\t\treturn \"")).output(mark("name")).output(literal("\";\n\t\t}\n\n\t\t@Override\n\t\tpublic Scale scale() {\n\t\t\treturn Scale.")).output(mark("scale")).output(literal(";\n\t\t}\n\n\t\t@Override\n\t\tpublic Query<EntityDefinition> entities() {\n\t\t\treturn new Query<EntityDefinition>(List.of(")).output(expression().output(mark("entity", "definition").multiple(","))).output(literal("));\n\t\t}\n\n\t\t@Override\n\t\tpublic Query<StructDefinition> structs() {\n\t\t\treturn new Query<StructDefinition>(List.of(")).output(expression().output(mark("struct", "definition").multiple(","))).output(literal("));\n\t\t}\n\n\t\t@Override\n\t\tpublic Optional<EntityDefinition> entity(String name) {\n\t\t\tswitch(name) {\n\t\t\t\t")).output(expression().output(mark("entity", "defSwitchCase").multiple("\n"))).output(literal("\n\t\t\t}\n\t\t\treturn Optional.empty();\n\t\t}\n\n\t\t@Override\n    \tpublic Optional<StructDefinition> struct(String name) {\n    \t\tswitch(name) {\n    \t\t\t")).output(expression().output(mark("struct", "defSwitchCase").multiple("\n"))).output(literal("\n    \t\t}\n    \t\treturn Optional.empty();\n    \t}\n\n\t\tprivate ")).output(mark("name", "FirstUpperCase")).output(literal("DatamartDefinition datamart() {\n\t\t\treturn this;\n\t\t}\n\n\t\t")).output(expression().output(mark("entity", "declareDefinition").multiple("\n"))).output(literal("\n\n        ")).output(expression().output(mark("struct", "declareDefinition").multiple("\n"))).output(literal("\n\t}\n\n\tstatic {\n\t\ttry {\n\t\t\tObject ref = ")).output(mark("name", "firstUpperCase")).output(literal("Datamart.class.getDeclaredField(\"definition\").get(null);\n\t\t\tjava.lang.reflect.Field field = ref.getClass().getDeclaredField(\"definition\");\n\t\t\tfield.setAccessible(true);\n\t\t\tfield.set(ref, definition);\n\t\t\tfield.setAccessible(false);\n\t\t} catch (Exception e) {\n\t\t\tthrow new ExceptionInInitializerError(\"Could not set ")).output(mark("name", "firstUpperCase")).output(literal("Datamart.definition field\");\n\t\t}\n\t}\n}")),
				rule().condition((type("entity")), (trigger("definition"))).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition")),
				rule().condition((type("struct")), (trigger("definition"))).output(mark("fullName", "firstLowerCase")).output(literal("StructDefinition")),
				rule().condition((type("entity")), (trigger("defswitchcase"))).output(literal("case \"")).output(mark("name", "FirstUpperCase")).output(literal("\": return Optional.of(")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition);")),
				rule().condition((type("struct")), (trigger("defswitchcase"))).output(literal("case \"")).output(mark("fullName", "FirstUpperCase")).output(literal("\": return Optional.of(")).output(mark("fullName", "firstLowerCase")).output(literal("StructDefinition);")),
				rule().condition((type("entity")), (trigger("declaredefinition"))).output(literal("public final EntityDefinition ")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition = new EntityDefinition() {\n\tprivate final List<AttributeDefinition> declaredAttributes = initAttributeDefinitions();\n\tpublic String fullName() {return \"")).output(mark("fullName")).output(literal("\";}\n\tpublic String name() {return \"")).output(mark("name")).output(literal("\";}\n\tpublic boolean isAbstract() {return ")).output(mark("isAbstract")).output(literal(";}\n\tpublic List<AttributeDefinition> declaredAttributes() {\treturn declaredAttributes;}\n\tpublic Optional<EntityDefinition> parent() {return ")).output(expression().output(literal("Optional.of(")).output(mark("parent", "firstLowerCase")).output(literal("EntityDefinition)")).next(expression().output(literal("Optional.empty()")))).output(literal(";}\n\tpublic List<EntityDefinition> ancestors() {return java.util.List.of(")).output(expression().output(mark("ancestor", "definition").multiple(","))).output(literal(");}\n\tpublic List<EntityDefinition> descendants() {return java.util.List.of(")).output(expression().output(mark("descendant", "definition").multiple(","))).output(literal(");}\n\tpublic Class<?> javaClass() {return ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(".class;}\n\tprivate List<AttributeDefinition> initAttributeDefinitions() {\n\t\tList<AttributeDefinition> list = new ArrayList<>();\n\t\t")).output(expression().output(mark("hasNoParents", "addIdAndEnabledAttributes"))).output(literal("\n    \t")).output(expression().output(mark("attribute", "addDefinition").multiple("\n"))).output(literal("\n    \treturn Collections.synchronizedList(list);\n\t}\n\tpublic boolean equals(Object other) {\n\t\tif(other == null || other.getClass() != getClass()) return false;\n\t\treturn fullName().equals(((EntityDefinition)other).fullName());\n\t}\n\tpublic int hashCode() {return fullName().hashCode();}\n\tpublic String toString() {return fullName();}\n};")),
				rule().condition((type("struct")), (trigger("declaredefinition"))).output(literal("public final StructDefinition ")).output(mark("fullName", "firstLowerCase")).output(literal("StructDefinition = new StructDefinition() {\n\tprivate final List<AttributeDefinition> declaredAttributes = initAttributeDefinitions();\n\tpublic String fullName() {return \"")).output(mark("fullName")).output(literal("\";}\n\tpublic String name() {return \"")).output(mark("name")).output(literal("\";}\n\tpublic List<AttributeDefinition> declaredAttributes() {return declaredAttributes;}\n\tpublic Optional<StructDefinition> parent() {return ")).output(expression().output(literal("Optional.of(")).output(mark("parent", "firstLowerCase")).output(literal("StructDefinition)")).next(expression().output(literal("Optional.empty()")))).output(literal(";}\n\tpublic List<StructDefinition> ancestors() {return java.util.List.of(")).output(expression().output(mark("ancestor", "definition").multiple(","))).output(literal(");}\n\tpublic List<StructDefinition> descendants() {return java.util.List.of(")).output(expression().output(mark("descendant", "definition").multiple(","))).output(literal(");}\n\tpublic Class<?> javaClass() {return ")).output(mark("package")).output(literal(".")).output(mark("name", "FirstUpperCase")).output(literal(".class;}\n\tprivate List<AttributeDefinition> initAttributeDefinitions() {\n\t\tList<AttributeDefinition> list = new ArrayList<>(")).output(mark("numAttributes")).output(literal(");\n    \t")).output(expression().output(mark("attribute", "addDefinition").multiple("\n"))).output(literal("\n    \treturn Collections.synchronizedList(list);\n\t}\n\tpublic boolean equals(Object other) {\n    \tif(other == null || other.getClass() != getClass()) return false;\n    \treturn fullName().equals(((StructDefinition)other).fullName());\n    }\n    public int hashCode() {return fullName().hashCode();}\n    public String toString() {return fullName();}\n};")),
				rule().condition((type("subclass")), (trigger("name"))).output(literal("\"")).output(mark("name", "FirstUpperCase")).output(literal("\"")),
				rule().condition((type("entity")), (anyTypes("descendant", "ancestor")), (trigger("definition"))).output(mark("name", "FirstUpperCase")).output(literal("EntityDefinition")),
				rule().condition((type("struct")), (anyTypes("descendant", "ancestor")), (trigger("definition"))).output(mark("name", "FirstUpperCase")).output(literal("StructDefinition")),
				rule().condition((trigger("addidandenabledattributes"))).output(literal("list.add(new AttributeDefinition() {\n\tpublic String name() {return \"id\";}\n\tpublic Class<?> type() {return String.class;}\n\tpublic String toString() {return name();}\n});\nlist.add(new AttributeDefinition() {\n\tpublic String name() {return \"enabled\";}\n\tpublic Class<?> type() {return Boolean.class;}\n\tpublic String toString() {return name();}\n});")),
			rule().condition((type("attribute")), not(type("inherited")), (type("collection")), (trigger("adddefinition"))).output(literal("list.add(new AttributeDefinition() {\n\tpublic String name() {return \"")).output(mark("name", "firstLowerCase")).output(literal("\";}\n\tpublic Class<?> type() {return ")).output(mark("type")).output(literal(".class;}\n\tpublic String toString() {return name();}\n\tpublic List<ParameterDefinition> parameters() {\n\t\treturn List.of(new ParameterDefinition() {\n\t\t\tpublic Optional<ConceptDefinition<?>> asConceptDefinition() {return ")).output(mark("parameter", "asConceptDefinition")).output(literal(";}\n\t\t\tpublic Class<?> javaClass() {return ")).output(mark("parameterType")).output(literal(".class;}\n\t\t\tpublic String toString() {return javaClass().getSimpleName();}\n\t\t});\n\t}\n});")),
			rule().condition((type("attribute")), not(type("inherited")), (trigger("adddefinition"))).output(literal("list.add(new AttributeDefinition() {\n\tpublic String name() {return \"")).output(mark("name", "firstLowerCase")).output(literal("\";}\n\tpublic Class<?> type() {return ")).output(mark("type")).output(literal(".class;}\n\tpublic String toString() {return name();}\n});")),
			rule().condition((type("parameter")), (type("entity")), (trigger("asconceptdefinition"))).output(literal("Optional.of(datamart().")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition)")),
			rule().condition((type("parameter")), (type("struct")), (trigger("asconceptdefinition"))).output(literal("Optional.of(datamart().")).output(mark("name", "firstLowerCase")).output(literal("StructDefinition)")),
			rule().condition((type("parameter")), (trigger("asconceptdefinition"))).output(literal("Optional.empty()")),
			rule().condition(not(type("abstract")), (type("entity")), (trigger("registermounter"))).output(literal("mounters.computeIfAbsent(\"")).output(mark("event", "firstUpperCase")).output(literal("\", type -> new ArrayList<>(1)).add(new ")).output(mark("package")).output(literal(".mounters.")).output(mark("name", "FirstUpperCase")).output(literal("Mounter(entities, entityListeners));")),
			rule().condition((type("subclass")), (trigger("getallstream"))).output(mark("name", "Plural", "firstLowerCase")).output(literal("()")),
			rule().condition((type("timeline")), (trigger("getter"))).output(literal("@Override\npublic TimelineNode ")).output(mark("name", "FirstLowerCase")).output(literal("Timeline(String id) {\n\treturn timelines.computeIfAbsent(id + \":")).output(mark("name")).output(literal("\", theId -> new TimelineNodeImpl(id, \"")).output(mark("name")).output(literal("\", Set.of(")).output(mark("sources")).output(literal(")));\n}")),
			rule().condition((type("reel")), (trigger("getter"))).output(literal("@Override\npublic ReelNode ")).output(mark("name", "FirstLowerCase")).output(literal("Reel(String id) {\n\treturn reels.computeIfAbsent(id + \":")).output(mark("name")).output(literal("\", theId -> new ReelNodeImpl(id, \"")).output(mark("name")).output(literal("\", Set.of(")).output(mark("sources")).output(literal(")));\n}")),
			rule().condition((trigger("timelinesbyidmethod"))).output(literal("@Override\npublic Stream<TimelineNode> timelines(String id) {\n\tString[] files = listTimelineFilesOf(id);\n\treturn (Stream<TimelineNode>) Arrays.stream(files)\n\t\t.filter(f -> f != null && !f.isEmpty())\n\t\t.<TimelineNode>map(f -> {\n\t\t\ttry {\n\t\t\t\tFile file = new File(f);\n            \tString type = file.getParentFile().getName();\n            \tTimelineNodeImpl node = timelines.get(id + \":\" + type);\n            \tif(node == null) {\n            \t\tnode = new TimelineNodeImpl(id, type, sourcesOfTimeline(type), file.exists() ? file : null);\n            \t\ttimelines.put(id + \":\" + type, node);\n            \t}\n            \treturn node;\n\t\t\t} catch(Exception ignored) {\n\t\t\t\treturn null;\n\t\t\t}\n\t\t}).filter(java.util.Objects::nonNull);\n}\n\nprivate String[] listTimelineFilesOf(String id) {\n\ttry {\n\t\tActiveMQTextMessage request = new ActiveMQTextMessage();\n\t\trequest.setText(\"datamart=\" + name() + \";operation=list-timelines;id=\" + id);\n\t\tjavax.jms.Message message = requestResponseFromDatahub(\"list-timelines=\" + id, request);\n\t\treturn ((javax.jms.TextMessage)message).getText().split(\",\");\n\t} catch(Exception e) {\n\t\tLogger.error(e);\n\t\treturn new String[0];\n\t}\n}")),
			rule().condition((trigger("reelsbyidmethod"))).output(literal("@Override\npublic Stream<ReelNode> reels(String id) {\n\tString[] files = listReelFilesOf(id);\n\treturn (Stream<ReelNode>) Arrays.stream(files)\n\t\t.filter(f -> f != null && !f.isEmpty())\n\t\t.<ReelNode>map(f -> {\n\t\t\ttry {\n\t\t\t\tFile file = new File(f);\n            \tString type = file.getParentFile().getName();\n            \tReelNodeImpl node = reels.get(id + \":\" + type);\n            \tif(node == null) {\n            \t\tnode = new ReelNodeImpl(id, type, sourcesOfReel(type), file.exists() ? file : null);\n            \t\treels.put(id + \":\" + type, node);\n            \t}\n            \treturn node;\n\t\t\t} catch(Exception ignored) {\n\t\t\t\treturn null;\n\t\t\t}\n\t\t}).filter(java.util.Objects::nonNull);\n}\n\nprivate String[] listReelFilesOf(String id) {\n\ttry {\n\t\tActiveMQTextMessage request = new ActiveMQTextMessage();\n\t\trequest.setText(\"datamart=\" + name() + \";operation=list-reels;id=\" + id);\n\t\tjavax.jms.Message message = requestResponseFromDatahub(\"list-reels=\" + id, request);\n\t\treturn ((javax.jms.TextMessage)message).getText().split(\",\");\n\t} catch(Exception e) {\n\t\tLogger.error(e);\n\t\treturn new String[0];\n\t}\n}")),
			rule().condition((trigger("sourcesswitchcase"))).output(literal("case \"")).output(mark("name")).output(literal("\" -> Set.of(")).output(mark("sources")).output(literal(");")),
			rule().condition((type("abstract")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\treturn id == null ? null : entities.getDescendant(definition.")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition, id);\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn Stream.of(\n\t\t")).output(mark("subclass", "getAllStream").multiple(",\n")).output(literal("\n\t).<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(">flatMap(java.util.function.Function.identity());//.distinct();\n}")),
			rule().condition((type("superclass")), not(type("abstract")), (trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\treturn id == null ? null : entities.getDescendant(definition.")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition, id);\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn Stream.of(\n\t\tentities.<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(">stream(definition.")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition),\n\t\t")).output(mark("subclass", "getAllStream").multiple(",\n")).output(literal("\n\t).<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(">flatMap(java.util.function.Function.identity());//.distinct();\n}")),
			rule().condition((trigger("getter"))).output(literal("@Override\npublic ")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id) {\n\treturn id == null ? null : entities.get(definition.")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition, id);\n}\n\n@Override\npublic Stream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("() {\n\treturn entities.stream(definition.")).output(mark("name", "firstLowerCase")).output(literal("EntityDefinition);\n}")),
			rule().condition((type("entity")), (trigger("gettersignature"))).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal(" ")).output(mark("name", "firstLowerCase")).output(literal("(String id);\nStream<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("();\ndefault List<")).output(mark("package")).output(literal(".entities.")).output(mark("name", "FirstUpperCase")).output(literal("> ")).output(mark("name", "firstLowerCase")).output(literal("List() {return ")).output(mark("name", "Plural", "firstLowerCase")).output(literal("().collect(Collectors.toList());}")),
			rule().condition((type("timeline")), (trigger("gettersignature"))).output(literal("default TimelineNode ")).output(mark("name", "firstLowerCase")).output(literal("Timeline(")).output(mark("package")).output(literal(".entities.")).output(mark("entity", "firstUpperCase")).output(literal(" entity) {return ")).output(mark("name", "firstLowerCase")).output(literal("Timeline(entity.id());}\nTimelineNode ")).output(mark("name", "firstLowerCase")).output(literal("Timeline(String id);")),
			rule().condition((type("reel")), (trigger("gettersignature"))).output(literal("default ReelNode ")).output(mark("name", "firstLowerCase")).output(literal("Reel(")).output(mark("package")).output(literal(".entities.")).output(mark("entity", "firstUpperCase")).output(literal(" entity) {return ")).output(mark("name", "firstLowerCase")).output(literal("Reel(entity.id());}\nReelNode ")).output(mark("name", "firstLowerCase")).output(literal("Reel(String id);"))
		);
	}
}