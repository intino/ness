//package org.example.test.model;
//
//import com.hazelcast.client.HazelcastClient;
//import com.hazelcast.client.config.ClientConfig;
//import com.hazelcast.client.config.ClientNetworkConfig;
//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.map.IMap;
//import com.hazelcast.topic.Message;
//import com.hazelcast.topic.MessageListener;
//import io.intino.alexandria.logger.Logger;
//import io.intino.ness.master.data.EntityListener;
//import io.intino.ness.master.data.EntityListener.Event;
//import io.intino.ness.master.messages.ListenerMasterMessage;
//import io.intino.ness.master.messages.MasterMessagePublisher;
//import io.intino.ness.master.messages.MasterMessageSerializer;
//import io.intino.ness.master.messages.UpdateMasterMessage;
//import io.intino.ness.master.model.Entity;
//import io.intino.ness.master.model.Triplet;
//import io.intino.ness.master.model.TripletRecord;
//import io.intino.ness.master.serialization.MasterSerializer;
//import io.intino.ness.master.serialization.MasterSerializers;
//import org.example.test.model.entities.*;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.logging.ConsoleHandler;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.LogManager;
//import java.util.stream.Stream;
//
//import static io.intino.ness.master.core.Master.MASTER_MAP_NAME;
//import static io.intino.ness.master.core.Master.METADATA_MAP_NAME;
//import static io.intino.ness.master.messages.MasterTopics.MASTER_LISTENER_TOPIC;
//import static io.intino.ness.master.messages.MasterTopics.MASTER_UPDATE_TOPIC;
//import static java.util.Objects.requireNonNull;
//
//public class FullLoadMasterTerminal implements MasterTerminal {
//
//	private final Map<String, Employee> employeeMap = new ConcurrentHashMap<>();
//	private final Map<String, Country> countryMap = new ConcurrentHashMap<>();
//	private final Map<String, Area> areaMap = new ConcurrentHashMap<>();
//	private final Map<String, Region> regionMap = new ConcurrentHashMap<>();
//	private final Map<String, Theater> theaterMap = new ConcurrentHashMap<>();
//	private final Map<String, Screen> screenMap = new ConcurrentHashMap<>();
//	private final Map<String, Dock> dockMap = new ConcurrentHashMap<>();
//	private final Map<String, ScreenDock> screenDockMap = new ConcurrentHashMap<>();
//	private final Map<String, Depot> depotMap = new ConcurrentHashMap<>();
//	private final Map<String, Office> officeMap = new ConcurrentHashMap<>();
//	private final Map<String, Desk> deskMap = new ConcurrentHashMap<>();
//	private final Map<String, Asset> assetMap = new ConcurrentHashMap<>();
//	private final Map<String, DualAsset> dualAssetMap = new ConcurrentHashMap<>();
//	private final Map<String, CheckField> checkFieldMap = new ConcurrentHashMap<>();
//	private final Map<String, OrderType> orderTypeMap = new ConcurrentHashMap<>();
//
//	private final AtomicBoolean initialized = new AtomicBoolean(false);
//	private final MasterTerminal.Config config;
//	protected HazelcastInstance hazelcast;
//	@SuppressWarnings("rawtypes")
//	private final Map<String, List<EntityListener>> entityListeners = new HashMap<>();
//
//	public FullLoadMasterTerminal(MasterTerminal.Config config) {
//		this.config = requireNonNull(config);
//	}
//
//	@Override
//	public void start() {
//		if(!initialized.compareAndSet(false, true)) return;
//		configureLogger();
//		initHazelcastClient();
//		loadData();
//		initListeners();
//	}
//
//	@Override
//	public void stop() {
//		hazelcast.shutdown();
//	}
//
//	@Override
//	public <T extends Entity> void addEntityListener(String type, EntityListener<T> listener) {
//		if(type == null) throw new NullPointerException("Type cannot be null");
//		if(listener == null) throw new NullPointerException("EntryListener cannot be null");
//		entityListeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
//	}
//
//	@Override
//	public MasterSerializer serializer() {
//		IMap<String, String> metadata = hazelcast.getMap(METADATA_MAP_NAME);
//		return MasterSerializers.get(metadata.get("serializer"));
//	}
//
//	@Override
//	public MasterTerminal.Config config() {
//		return new MasterTerminal.Config(config);
//	}
//
//	@Override
//	public Employee employee(String id) {
//		return employeeMap.get(id);
//	}
//
//	@Override
//	public Stream<Employee> employees() {
//		return employeeMap.values().stream();
//	}
//
//	@Override
//	public Place place(String id) {
//		switch(Triplet.typeOf(id)) {
//			case "country": return country(id);
//			case "area": return area(id);
//			case "region": return region(id);
//			case "theater": return theater(id);
//			case "screen": return screen(id);
//			case "dock": return dock(id);
//			case "depot": return depot(id);
//			case "office": return office(id);
//			case "desk": return desk(id);
//		}
//		return null;
//	}
//
//	@Override
//	public Stream<Place> places() {
//		return Stream.of(
//				countries(),
//				areas(),
//				regions(),
//				theaters(),
//				screens(),
//				docks(),
//				depots(),
//				offices(),
//				desks()
//		).flatMap(java.util.function.Function.identity());
//	}
//
//	@Override
//	public Country country(String id) {
//		return countryMap.get(id);
//	}
//
//	@Override
//	public Stream<Country> countries() {
//		return countryMap.values().stream();
//	}
//
//	@Override
//	public Area area(String id) {
//		return areaMap.get(id);
//	}
//
//	@Override
//	public Stream<Area> areas() {
//		return areaMap.values().stream();
//	}
//
//	@Override
//	public Region region(String id) {
//		return regionMap.get(id);
//	}
//
//	@Override
//	public Stream<Region> regions() {
//		return regionMap.values().stream();
//	}
//
//	@Override
//	public Theater theater(String id) {
//		return theaterMap.get(id);
//	}
//
//	@Override
//	public Stream<Theater> theaters() {
//		return theaterMap.values().stream();
//	}
//
//	@Override
//	public Screen screen(String id) {
//		return screenMap.get(id);
//	}
//
//	@Override
//	public Stream<Screen> screens() {
//		return screenMap.values().stream();
//	}
//
//	@Override
//	public Dock dock(String id) {
//		return dockMap.get(id);
//	}
//
//	@Override
//	public Stream<Dock> docks() {
//		return dockMap.values().stream();
//	}
//
//	@Override
//	public ScreenDock screenDock(String id) {
//		return screenDockMap.get(id);
//	}
//
//	@Override
//	public Stream<ScreenDock> screenDocks() {
//		return screenDockMap.values().stream();
//	}
//
//	@Override
//	public Depot depot(String id) {
//		return depotMap.get(id);
//	}
//
//	@Override
//	public Stream<Depot> depots() {
//		return depotMap.values().stream();
//	}
//
//	@Override
//	public Office office(String id) {
//		return officeMap.get(id);
//	}
//
//	@Override
//	public Stream<Office> offices() {
//		return officeMap.values().stream();
//	}
//
//	@Override
//	public Desk desk(String id) {
//		return deskMap.get(id);
//	}
//
//	@Override
//	public Stream<Desk> desks() {
//		return deskMap.values().stream();
//	}
//
//	@Override
//	public Asset asset(String id) {
//		return assetMap.get(id);
//	}
//
//	@Override
//	public Stream<Asset> assets() {
//		return assetMap.values().stream();
//	}
//
//	@Override
//	public DualAsset dualAsset(String id) {
//		return dualAssetMap.get(id);
//	}
//
//	@Override
//	public Stream<DualAsset> dualAssets() {
//		return dualAssetMap.values().stream();
//	}
//
//	@Override
//	public CheckField checkField(String id) {
//		return checkFieldMap.get(id);
//	}
//
//	@Override
//	public Stream<CheckField> checkFields() {
//		return checkFieldMap.values().stream();
//	}
//
//	@Override
//	public OrderType orderType(String id) {
//		return orderTypeMap.get(id);
//	}
//
//	@Override
//	public Stream<OrderType> orderTypes() {
//		return orderTypeMap.values().stream();
//	}
//
//	@Override
//	public void enable(String id) {
//		if(!config.allowWriting()) throw new UnsupportedOperationException("This master client is configured as read only");
//		if(id == null) throw new NullPointerException("Entity id cannot be null");
//		MasterMessagePublisher.publishMessage(hazelcast, MASTER_UPDATE_TOPIC, createMessage(UpdateMasterMessage.Action.Enable, id));
//	}
//
//	@Override
//	public void disable(String id) {
//		if(!config.allowWriting()) throw new UnsupportedOperationException("This master client is configured as read only");
//		if(id == null) throw new NullPointerException("Entity id cannot be null");
//		MasterMessagePublisher.publishMessage(hazelcast, MASTER_UPDATE_TOPIC, createMessage(UpdateMasterMessage.Action.Disable, id));
//	}
//
//	@Override
//	public void publish(Entity entity) {
//		if(!config.allowWriting()) throw new UnsupportedOperationException("This master client is configured as read only");
//		if(entity == null) throw new NullPointerException("Entity cannot be null");
//		MasterMessagePublisher.publishMessage(hazelcast, MASTER_UPDATE_TOPIC, createMessage(UpdateMasterMessage.Action.Publish, serializer().serialize(entity.asTripletRecord())));
//	}
//
//	@Override
//	public MasterView disabled() {
//		return new DisabledView(this);
//	}
//
//	private UpdateMasterMessage createMessage(UpdateMasterMessage.Action action, String value) {
//		return new UpdateMasterMessage(config.instanceName(), action, value, java.time.Instant.now());
//	}
//
//	private boolean isFiltered(TripletRecord record) {
//		if(config.filter() == MasterTerminal.EntityFilter.All) return false;
//		final boolean enabled = isEnabled(record);
//		return (enabled && config.filter() == MasterTerminal.EntityFilter.OnlyEnabled)
//				|| (!enabled && config.filter() == MasterTerminal.EntityFilter.OnlyDisabled);
//	}
//
//	private boolean isEnabled(TripletRecord record) {
//		String enabledValue = record.getValue("enabled");
//		return enabledValue == null || "true".equalsIgnoreCase(enabledValue);
//	}
//
//	protected void add(TripletRecord record) {
//		if(isFiltered(record)) return;
//		switch(record.type()) {
//			case "employee": addToEmployee(record); break;
//			case "country": addToCountry(record); break;
//			case "area": addToArea(record); break;
//			case "region": addToRegion(record); break;
//			case "theater": addToTheater(record); break;
//			case "screen": addToScreen(record); break;
//			case "dock": addToDock(record); break;
//			case "screendock": addToScreenDock(record); break;
//			case "depot": addToDepot(record); break;
//			case "office": addToOffice(record); break;
//			case "desk": addToDesk(record); break;
//			case "asset": addToAsset(record); break;
//			case "dualasset": addToDualAsset(record); break;
//			case "checkfield": addToCheckField(record); break;
//			case "ordertype": addToOrderType(record); break;
//		}
//	}
//
//	protected void remove(String id) {
//		switch(Triplet.typeOf(id)) {
//			case "employee": removeFromEmployee(id); break;
//			case "country": removeFromCountry(id); break;
//			case "area": removeFromArea(id); break;
//			case "region": removeFromRegion(id); break;
//			case "theater": removeFromTheater(id); break;
//			case "screen": removeFromScreen(id); break;
//			case "dock": removeFromDock(id); break;
//			case "screendock": removeFromScreenDock(id); break;
//			case "depot": removeFromDepot(id); break;
//			case "office": removeFromOffice(id); break;
//			case "desk": removeFromDesk(id); break;
//			case "asset": removeFromAsset(id); break;
//			case "dualasset": removeFromDualAsset(id); break;
//			case "checkfield": removeFromCheckField(id); break;
//			case "ordertype": removeFromOrderType(id); break;
//		}
//	}
//
//	private void addToEmployee(TripletRecord record) {
//		Employee entity = new Employee(record.id(), this);
//		record.triplets().forEach(entity::add);
//		employeeMap.put(record.id(), entity);
//	}
//
//	private void addToCountry(TripletRecord record) {
//		Country entity = new Country(record.id(), this);
//		record.triplets().forEach(entity::add);
//		countryMap.put(record.id(), entity);
//	}
//
//	private void addToArea(TripletRecord record) {
//		Area entity = new Area(record.id(), this);
//		record.triplets().forEach(entity::add);
//		areaMap.put(record.id(), entity);
//	}
//
//	private void addToRegion(TripletRecord record) {
//		Region entity = new Region(record.id(), this);
//		record.triplets().forEach(entity::add);
//		regionMap.put(record.id(), entity);
//	}
//
//	private void addToTheater(TripletRecord record) {
//		Theater entity = new Theater(record.id(), this);
//		record.triplets().forEach(entity::add);
//		theaterMap.put(record.id(), entity);
//	}
//
//	private void addToScreen(TripletRecord record) {
//		Screen entity = new Screen(record.id(), this);
//		record.triplets().forEach(entity::add);
//		screenMap.put(record.id(), entity);
//	}
//
//	private void addToDock(TripletRecord record) {
//		Dock entity = new Dock(record.id(), this);
//		record.triplets().forEach(entity::add);
//		dockMap.put(record.id(), entity);
//	}
//
//	private void addToScreenDock(TripletRecord record) {
//		ScreenDock entity = new ScreenDock(record.id(), this);
//		record.triplets().forEach(entity::add);
//		screenDockMap.put(record.id(), entity);
//	}
//
//	private void addToDepot(TripletRecord record) {
//		Depot entity = new Depot(record.id(), this);
//		record.triplets().forEach(entity::add);
//		depotMap.put(record.id(), entity);
//	}
//
//	private void addToOffice(TripletRecord record) {
//		Office entity = new Office(record.id(), this);
//		record.triplets().forEach(entity::add);
//		officeMap.put(record.id(), entity);
//	}
//
//	private void addToDesk(TripletRecord record) {
//		Desk entity = new Desk(record.id(), this);
//		record.triplets().forEach(entity::add);
//		deskMap.put(record.id(), entity);
//	}
//
//	private void addToAsset(TripletRecord record) {
//		Asset entity = new Asset(record.id(), this);
//		record.triplets().forEach(entity::add);
//		assetMap.put(record.id(), entity);
//	}
//
//	private void addToDualAsset(TripletRecord record) {
//		DualAsset entity = new DualAsset(record.id(), this);
//		record.triplets().forEach(entity::add);
//		dualAssetMap.put(record.id(), entity);
//	}
//
//	private void addToCheckField(TripletRecord record) {
//		CheckField entity = new CheckField(record.id(), this);
//		record.triplets().forEach(entity::add);
//		checkFieldMap.put(record.id(), entity);
//	}
//
//	private void addToOrderType(TripletRecord record) {
//		OrderType entity = new OrderType(record.id(), this);
//		record.triplets().forEach(entity::add);
//		orderTypeMap.put(record.id(), entity);
//	}
//
//	private void removeFromEmployee(String id) {
//		employeeMap.remove(id);
//	}
//
//	private void removeFromCountry(String id) {
//		countryMap.remove(id);
//	}
//
//	private void removeFromArea(String id) {
//		areaMap.remove(id);
//	}
//
//	private void removeFromRegion(String id) {
//		regionMap.remove(id);
//	}
//
//	private void removeFromTheater(String id) {
//		theaterMap.remove(id);
//	}
//
//	private void removeFromScreen(String id) {
//		screenMap.remove(id);
//	}
//
//	private void removeFromDock(String id) {
//		dockMap.remove(id);
//	}
//
//	private void removeFromScreenDock(String id) {
//		screenDockMap.remove(id);
//	}
//
//	private void removeFromDepot(String id) {
//		depotMap.remove(id);
//	}
//
//	private void removeFromOffice(String id) {
//		officeMap.remove(id);
//	}
//
//	private void removeFromDesk(String id) {
//		deskMap.remove(id);
//	}
//
//	private void removeFromAsset(String id) {
//		assetMap.remove(id);
//	}
//
//	private void removeFromDualAsset(String id) {
//		dualAssetMap.remove(id);
//	}
//
//	private void removeFromCheckField(String id) {
//		checkFieldMap.remove(id);
//	}
//
//	private void removeFromOrderType(String id) {
//		orderTypeMap.remove(id);
//	}
//
//	private void initHazelcastClient() {
//		if(hazelcast != null) return;
//
//		ClientConfig config = new ClientConfig();
//		config.setInstanceName(this.config.instanceName());
//		config.setNetworkConfig(new ClientNetworkConfig().setAddresses(this.config.addresses()));
//		this.config.properties().forEach(config::setProperty);
//
//		hazelcast = HazelcastClient.newHazelcastClient(config);
//	}
//
//	protected void initListeners() {
//		hazelcast.getTopic(MASTER_LISTENER_TOPIC).addMessageListener(new ListenerTopicMessageListener());
//	}
//
//	private void loadData() {
//		IMap<String, String> master = hazelcast.getMap(MASTER_MAP_NAME);
//		MasterSerializer serializer = serializer();
//
//		Logger.debug("Loading data from master (serializer=" + serializer.name() + ")");
//		long start = System.currentTimeMillis();
//
//		if(config.multithreadLoading())
//			loadDataMultiThread(master, serializer);
//		else
//			loadDataSingleThread(master, serializer);
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data from master loaded in " + time + " ms");
//	}
//
//	private void loadDataSingleThread(IMap<String, String> master, MasterSerializer serializer) {
//		master.forEach((id, serializedRecord) -> add(serializer.deserialize(serializedRecord)));
//	}
//
//	private void loadDataMultiThread(IMap<String, String> master, MasterSerializer serializer) {
//		try {
//			ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
//
//			master.forEach((id, serializedRecord) -> threadPool.submit(() -> add(serializer.deserialize(serializedRecord))));
//
//			threadPool.shutdown();
//			threadPool.awaitTermination(1, TimeUnit.HOURS);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private static void configureLogger() {
//		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
//		rootLogger.setLevel(Level.WARNING);
//		for (Handler h : rootLogger.getHandlers()) rootLogger.removeHandler(h);
//		final ConsoleHandler handler = new ConsoleHandler();
//		handler.setLevel(Level.WARNING);
//		handler.setFormatter(new io.intino.alexandria.logger.Formatter());
//		rootLogger.setUseParentHandlers(false);
//		rootLogger.addHandler(handler);
//	}
//
//	private class ListenerTopicMessageListener implements MessageListener<Object> {
//
//		@Override
//		public void onMessage(Message<Object> rawMessage) {
//			ListenerMasterMessage message = MasterMessageSerializer.deserialize(String.valueOf(rawMessage.getMessageObject()), ListenerMasterMessage.class);
//			TripletRecord record = serializer().deserialize(message.record());
//			Event<?> event = new MasterEntityEvent<>(message.author(), asEventType(message.action()), asEntity(record), message.ts(), message.id());
//			process(event, record);
//			notifyEntityListeners(event);
//		}
//
//		protected void process(Event<?> event, TripletRecord record) {
//			switch(event.type()) {
//				case Create:
//				case Update:
//				case Enable:
//					add(record);
//					break;
//				case Disable:
//				case Remove:
//					remove(record.id());
//					break;
//			}
//		}
//
//		private Event.Type asEventType(ListenerMasterMessage.Action action) {
//			switch(action) {
//				case Created: return Event.Type.Create;
//				case Updated: return Event.Type.Update;
//				case Enabled: return Event.Type.Enable;
//				case Disabled: return Event.Type.Disable;
//				case Removed: return Event.Type.Remove;
//				default: throw new IllegalArgumentException("Unknown listener action " + action);
//			}
//		}
//
//		@SuppressWarnings("all")
//		protected void notifyEntityListeners(Event<?> event) {
//			List<EntityListener> listeners = entityListeners.get(event.entity().id().type());
//			if(listeners != null) listeners.forEach(listener -> listener.notify(event));
//		}
//	}
//
//	private static class DisabledView extends FullLoadMasterTerminal {
//
//		public DisabledView(FullLoadMasterTerminal original) {
//			super(new Config(original.config()).allowWriting(false).filter(EntityFilter.OnlyDisabled), original.hazelcast);
//		}
//
//		@Override
//		public <T extends Entity> void addEntityListener(String type, EntityListener<T> listener) {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public void stop() {
//			throw new UnsupportedOperationException();
//		}
//
//		@Override
//		public MasterView disabled() {
//			return this;
//		}
//
//		@Override
//		protected void initListeners() {
//			hazelcast.getTopic(MASTER_LISTENER_TOPIC).addMessageListener(new DisabledListenerTopicMessageListener());
//		}
//
//		private class DisabledListenerTopicMessageListener extends ListenerTopicMessageListener {
//
//			protected void process(Event<?> event, TripletRecord record) {
//				switch(event.type()) {
//					case Create:
//					case Update:
//					case Enable:
//						remove(record.id());
//						break;
//					case Disable:
//					case Remove:
//						add(record);
//						break;
//				}
//			}
//
//			protected void notifyEntityListeners(Event<?> event) {}
//		}
//	}
//
//	public static class MasterEntityEvent<T extends Entity> implements Event<T> {
//
//		private final String author;
//		private final Type type;
//		private final T entity;
//		private final Instant ts;
//		private final String messageId;
//
//		private MasterEntityEvent(String author, Type type, T entity, Instant ts, String messageId) {
//			this.author = author;
//			this.type = type;
//			this.entity = entity;
//			this.ts = ts;
//			this.messageId = messageId;
//		}
//
//		@Override
//		public String author() {
//			return author;
//		}
//
//		@Override
//		public Type type() {
//			return type;
//		}
//
//		@Override
//		public T entity() {
//			return entity;
//		}
//
//		@Override
//		public Instant ts() {
//			return ts;
//		}
//
//		@Override
//		public String messageId() {
//			return messageId;
//		}
//	}
//}