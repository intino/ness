package io.intino.ness.master.core;

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.master.data.DatalakeLoader;
import io.intino.ness.master.io.TriplesFileWriter;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;
import io.intino.ness.master.serialization.MasterSerializers;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class Master {

	public static final String METADATA_MAP_NAME = "metadata";
	public static final String MASTER_MAP_NAME = "master";
	public static final String REQUESTS_TOPIC = "requests";
	public static final String MESSAGE_SEPARATOR = "##";
	public static final String NONE_TYPE = "";

	private HazelcastInstance hazelcast;
	private final Config config;
	private IMap<String, String> metadataMap;
	private IMap<String, String> masterMap;

	public Master(Config config) {
		this.config = requireNonNull(config);
		checkConfigValues();
	}

	public void start() {
		Logger.info("Initializing Master...");
		{
			Map<String, TripletRecord> data = loadData();
			initHazelcast();
			initMaps(data);
			setupListeners();
		}
		System.gc();
		Logger.info("Data loaded into Master:\n" + histogram());
		Logger.info("Master initialized. Using " + getHazelcastMemoryUsedMB() + " MB");
	}

	private String histogram() {
		Map<String, Integer> histogram = new HashMap<>();
		masterMap.keySet().stream().map(Triplet::typeOf).map(t -> "\"" + t + "\"").forEach(key -> histogram.compute(key, (k, v) -> v == null ? 1 : v + 1));
		return "  " + histogram.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n  "));
	}

	private void initHazelcast() {
		Logger.info("Initializing hazelcast instance...");
		hazelcast = Hazelcast.newHazelcastInstance(getHazelcastConfig());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> hazelcast.shutdown(), "Master-Shutdown"));
	}

	protected void initMaps(Map<String, TripletRecord> data) {
		MasterSerializer serializer = serializer();

		metadataMap = hazelcast.getMap(METADATA_MAP_NAME);
		metadataMap.set("instanceName", config.instanceName());
		metadataMap.set("port", String.valueOf(config.port()));
		metadataMap.set("host", config.host());
		metadataMap.set("serializer", serializer.name());
		metadataMap.set("datalakeRootPath", config.datalakeRootPath().getPath());

		masterMap = hazelcast.getMap(MASTER_MAP_NAME);

		masterMap.setAll(data.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), serializer.serialize(e.getValue())))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	protected Map<String, TripletRecord> loadData() {
		Logger.info("Loading data...");
		long start = System.currentTimeMillis();
		int numTriples;

		DatalakeLoader.LoadResult result = config.datalakeLoader().load(config.datalakeRootPath(), serializer());

		numTriples = (int) result.triplesRead();

		final long time = System.currentTimeMillis() - start;

		Logger.info("Data loaded after " + time + " ms."
				+ " Num records: " + result.records().size()
				+ ", Num triples: " + numTriples
		);

		return result.records();
	}

	protected void setupListeners() {
		hazelcast.getTopic(REQUESTS_TOPIC).addMessageListener(this::handleRequestMessage);
		Logger.info("Hazelcast initialized");
	}

	protected void handleRequestMessage(Message<Object> message) {
		String[] info = message.getMessageObject().toString().split(MESSAGE_SEPARATOR, -1);
		String publisher = info[0];
		String tripleLine = info[1];
		if(add(publisher, tripleLine))
			save(publisher, tripleLine);
	}

	public boolean add(String publisher, String tripleLine) {
		return add(publisher, new Triplet(tripleLine));
	}

	public boolean add(String publisher, Triplet triplet) {
		TripletRecord record = getRecord(triplet.subject());
		if(record.contains(triplet)) return false;

		record.put(triplet);
		updateRecord(triplet.subject(), record);

		save(publisher, triplet);
		return true;
	}

	protected void updateRecord(String id, TripletRecord record) {
		masterMap.set(id, serializer().serialize(record));
	}

	protected TripletRecord getRecord(String subject) {
		String serializedRecord = masterMap.get(subject);
		if(serializedRecord == null) return new TripletRecord(subject);
		return serializer().deserialize(serializedRecord);
	}

	private synchronized void save(String publisher, String tripleLine) {
		save(publisher, new Triplet(tripleLine));
	}

	protected synchronized void save(String publisher, Triplet triplet) {
		try(TriplesFileWriter writer = new TriplesFileWriter(config.datalakeRootPath(), publisher)) {
			writer.write(triplet);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public MasterSerializer serializer() {
		return config.serializer();
	}

	protected com.hazelcast.config.Config getHazelcastConfig() {
		com.hazelcast.config.Config hzConfig = new com.hazelcast.config.Config();
		hzConfig.setInstanceName(config.instanceName());
		hzConfig.setNetworkConfig(new NetworkConfig().setPort(config.port()));
		return hzConfig;
	}

	private void checkConfigValues() {
		if(config.instanceName() == null) throw new MasterInitializationException("Instance name cannot be null");
		if(config.datalakeRootPath() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.port() <= 0) throw new MasterInitializationException("Port is invalid");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
	}

	private float getHazelcastMemoryUsedMB() {
		long metadata = metadataMap.getLocalMapStats().getOwnedEntryMemoryCost();
		long data = masterMap.getLocalMapStats().getOwnedEntryMemoryCost();
		return (metadata + data) / 1024.0f / 1024.0f;
	}

	public static class Config {

		private File datalakeRootPath;
		private String instanceName = "master";
		private int port = 5701;
		private String host = "localhost";
		private MasterSerializer serializer = MasterSerializers.getDefault();
		private DatalakeLoader datalakeLoader = DatalakeLoader.createDefault();

		public Config() {
		}

		public Config(Map<String, String> arguments) {
			this.datalakeRootPath = new File(arguments.get("datalake_path"));
			this.instanceName = arguments.getOrDefault("master_instance_name", instanceName);
			this.port = Integer.parseInt(arguments.getOrDefault("port", String.valueOf(port)));
			this.serializer = MasterSerializers.get(arguments.getOrDefault("serializer", MasterSerializers.Standard.getDefault()));
			this.host = arguments.getOrDefault("host", host);
		}

		public Config(String[] args) {
			this(toMap(args));
		}

		public File datalakeRootPath() {
			return datalakeRootPath;
		}

		public Config datalakeRootPath(File datalakeRootPath) {
			this.datalakeRootPath = datalakeRootPath;
			return this;
		}

		public String instanceName() {
			return instanceName;
		}

		public Config instanceName(String instanceName) {
			this.instanceName = instanceName;
			return this;
		}

		public int port() {
			return port;
		}

		public Config port(int port) {
			this.port = port;
			return this;
		}

		public String host() {
			return host;
		}

		public Config host(String host) {
			this.host = host;
			return this;
		}

		public MasterSerializer serializer() {
			return serializer;
		}

		public Config serializer(MasterSerializer serializer) {
			this.serializer = serializer;
			return this;
		}

		private static Map<String, String> toMap(String[] args) {
			return Arrays.stream(args).map(s -> s.split("=")).collect(Collectors.toMap(
					s -> s[0].trim(),
					s -> s[1].trim()
			));
		}

		public DatalakeLoader datalakeLoader() {
			return datalakeLoader;
		}

		public Config datalakeLoader(DatalakeLoader datalakeLoader) {
			this.datalakeLoader = datalakeLoader;
			return this;
		}
	}
}