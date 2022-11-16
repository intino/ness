package io.intino.ness.master.core;

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.master.data.FileTripletLoader;
import io.intino.ness.master.data.MasterTripletsDigester;
import io.intino.ness.master.data.TripletLoader;
import io.intino.ness.master.messages.ErrorMasterMessage;
import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.MasterMessagePublisher;
import io.intino.ness.master.messages.handlers.UpdateMasterMessageHandler;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;
import io.intino.ness.master.serialization.MasterSerializers;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.ness.master.messages.MasterTopics.MASTER_ERROR_TOPIC;
import static io.intino.ness.master.messages.MasterTopics.MASTER_UPDATE_TOPIC;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class Master {

	public static final String METADATA_MAP_NAME = "metadata";
	public static final String MASTER_MAP_NAME = "master";
	public static final String NONE_TYPE = "";

	private HazelcastInstance hazelcast;
	private final Config config;
	private IMap<String, String> metadataMap;
	private IMap<String, String> masterMap;

	public Master(Config config) {
		this.config = requireNonNull(config);
		checkConfigValues();
	}

	public IMap<String, String> masterMap() {
		return masterMap;
	}

	public IMap<String, String> metadataMap() {
		return metadataMap;
	}

	public File datalakeRootPath() {
		return config.datalakeRootPath();
	}

	public String instanceName() {
		return config.instanceName();
	}

	public int port() {
		return config.port();
	}

	public String host() {
		return config.host();
	}

	public MasterTripletsDigester tripletsDigester() {
		return config.tripletsDigester();
	}

	public TripletLoader tripletLoader() {
		return config.tripletLoader();
	}

	public void start() {
		Logger.trace("Initializing Master...");
		{
			Map<String, TripletRecord> data = loadData();
			initHazelcast();
			initMaps(data);
			setupListeners();
		}
		System.gc();
		Logger.trace("Data loaded into Master:\n" + histogram());
		Logger.info("Master initialized. Using " + getHazelcastMemoryUsedMB() + " MB");
	}

	private void initHazelcast() {
		Logger.trace("Initializing hazelcast instance...");
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
		try {
			Logger.trace("Loading data...");
			long start = System.currentTimeMillis();

			MasterTripletsDigester.Result result = config.tripletsDigester().load(config.tripletLoader(), serializer());
			result.stats().put("Num records", result.records().size());

			final long time = System.currentTimeMillis() - start;

			Logger.debug("Data loaded after " + time + " ms. Stats:\n" + Json.toJsonPretty(result.stats().map()));

			return result.records();

		} catch (Exception e) {
			throw new MasterInitializationException("Could not load master data due to a " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	protected void setupListeners() {
		hazelcast.getTopic(MASTER_UPDATE_TOPIC).addMessageListener(this::handleRequestMessage);
	}

	protected void handleRequestMessage(Message<Object> hzMessage) {
		try {
			new UpdateMasterMessageHandler(this).handle(String.valueOf(hzMessage.getMessageObject()));
		} catch (MasterMessageException e) {
			Logger.error(e);
			notifyError(e);
		}
	}

	private void notifyError(MasterMessageException error) {
		try {
			MasterMessagePublisher.publishMessage(hazelcast, MASTER_ERROR_TOPIC, new ErrorMasterMessage(error, Instant.now()));
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	public MasterSerializer serializer() {
		return config.serializer();
	}

	protected com.hazelcast.config.Config getHazelcastConfig() {
		com.hazelcast.config.Config hzConfig = new com.hazelcast.config.Config();
		config.properties().forEach(hzConfig::setProperty);
		hzConfig.setInstanceName(config.instanceName());
		hzConfig.setNetworkConfig(new NetworkConfig().setPort(config.port()));
		return hzConfig;
	}

	private void checkConfigValues() {
		if(config.instanceName() == null) throw new MasterInitializationException("Instance name cannot be null");
		if(config.datalakeRootPath() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.host() == null) throw new MasterInitializationException("Host cannot be null");
		if(config.port() <= 0) throw new MasterInitializationException("Port is invalid");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
		if(config.tripletsDigester() == null) throw new MasterInitializationException("Triplet digester cannot be null");
		if(config.tripletLoader() == null) throw new MasterInitializationException("Triplet loader cannot be null");
	}

	private float getHazelcastMemoryUsedMB() {
		long metadata = metadataMap.getLocalMapStats().getOwnedEntryMemoryCost();
		long data = masterMap.getLocalMapStats().getOwnedEntryMemoryCost();
		return (metadata + data) / 1024.0f / 1024.0f;
	}

	private String histogram() {
		Map<String, Integer> histogram = new HashMap<>();
		masterMap.keySet().stream().map(Triplet::typeOf).map(t -> "\"" + t + "\"").forEach(key -> histogram.compute(key, (k, v) -> v == null ? 1 : v + 1));
		return "  " + histogram.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n  "));
	}

	public HazelcastInstance hazelcast() {
		return hazelcast;
	}

	public static class Config {

		public static final int DEFAULT_PORT = 5701;
		public static final String DEFAULT_INSTANCE_NAME = "master";
		public static final String DEFAULT_HOST = "localhost";
		public static final String DEFAULT_LOG_API = "log4j";

		private File datalakeRootPath;
		private String instanceName = DEFAULT_INSTANCE_NAME;
		private int port = DEFAULT_PORT;
		private String host = DEFAULT_HOST;
		private MasterSerializer serializer = MasterSerializers.getDefault();
		private MasterTripletsDigester tripletsDigester = MasterTripletsDigester.createDefault();
		private TripletLoader tripletLoader;
		private final Map<String, String> properties = new HashMap<>() {{
			put("hazelcast.logging.type", DEFAULT_LOG_API);
		}};

		public Config() {
		}

		public Config(Map<String, String> arguments) {
			this.datalakeRootPath = new File(arguments.get("datalake_path"));
			this.instanceName = arguments.getOrDefault("master_instance_name", instanceName);
			this.port = Integer.parseInt(arguments.getOrDefault("port", String.valueOf(port)));
			this.serializer = MasterSerializers.get(arguments.getOrDefault("serializer", MasterSerializers.Standard.getDefault()));
			this.host = arguments.getOrDefault("host", host);
			this.tripletLoader = new FileTripletLoader(datalakeRootPath);
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
			this.instanceName = instanceName == null ? DEFAULT_INSTANCE_NAME : instanceName;
			return this;
		}

		public int port() {
			return port;
		}

		public Config port(Integer port) {
			this.port = port == null ? DEFAULT_PORT : port;
			return this;
		}

		public String host() {
			return host;
		}

		public Config host(String host) {
			this.host = host == null ? DEFAULT_HOST : host;
			return this;
		}

		public MasterSerializer serializer() {
			return serializer;
		}

		public Config serializer(MasterSerializer serializer) {
			this.serializer = serializer == null ? MasterSerializers.getDefault() : serializer;
			return this;
		}

		private static Map<String, String> toMap(String[] args) {
			return Arrays.stream(args).map(s -> s.split("=")).collect(Collectors.toMap(
					s -> s[0].trim(),
					s -> s[1].trim()
			));
		}

		public MasterTripletsDigester tripletsDigester() {
			return tripletsDigester;
		}

		public Config tripletsDigester(MasterTripletsDigester digester) {
			this.tripletsDigester = digester;
			return this;
		}

		public TripletLoader tripletLoader() {
			return tripletLoader;
		}

		public Config tripletsLoader(TripletLoader tripletLoader) {
			this.tripletLoader = tripletLoader;
			return this;
		}

		public Map<String, String> properties() {
			return properties;
		}

		public Config putProperty(String key, String value) {
			this.properties.put(key, value);
			return this;
		}
	}
}