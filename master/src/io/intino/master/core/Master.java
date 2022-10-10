package io.intino.master.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.master.data.DatalakeLoader;
import io.intino.master.data.RecordTransformer;
import io.intino.master.io.TriplesFileReader;
import io.intino.master.io.TriplesFileWriter;
import io.intino.master.model.Triple;
import io.intino.master.model.TripleRecord;
import io.intino.master.serialization.MasterSerializer;

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
	private final MasterConfig config;
	private IMap<String, String> metadataMap;
	private IMap<String, String> masterMap;

	public Master(MasterConfig config) {
		this.config = requireNonNull(config);
		checkConfigValues();
	}

	public void start() {
		Logger.info("Initializing Master...");
		{
			Map<String, TripleRecord> data = loadData();
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
		masterMap.keySet().stream().map(Triple::typeOf).map(t -> "\"" + t + "\"").forEach(key -> histogram.compute(key, (k, v) -> v == null ? 1 : v + 1));
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

	protected void initMaps(Map<String, TripleRecord> data) {
		MasterSerializer serializer = serializer();

		metadataMap = hazelcast.getMap(METADATA_MAP_NAME);
		metadataMap.set("instanceName", config.instanceName());
		metadataMap.set("port", String.valueOf(config.port()));
		metadataMap.set("host", config.host());
		metadataMap.set("serializer", serializer.name());
		metadataMap.set("dataDirectory", config.dataDirectory().getPath());
		metadataMap.set("logDirectory", config.logDirectory().getPath());

		masterMap = hazelcast.getMap(MASTER_MAP_NAME);

		masterMap.setAll(data.entrySet().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), serializer.serialize(e.getValue().attributes())))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	protected Map<String, TripleRecord> loadData() {
		Logger.info("Loading data...");
		long start = System.currentTimeMillis();
		int numTriples;

		DatalakeLoader.LoadResult result = config.datalakeLoader().load(config.dataDirectory(), serializer());

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

	protected List<Triple> readTriplesIn(File folder) {
		return new TriplesFileReader(folder).triples();
	}

	protected void handleRequestMessage(Message<Object> message) {
		String[] info = message.getMessageObject().toString().split(MESSAGE_SEPARATOR, -1);
		String publisher = info[0];
		String tripleLine = info[1];
		if(add(publisher, tripleLine))
			save(publisher, tripleLine);
	}

	public boolean add(String publisher, String tripleLine) {
		return add(publisher, new Triple(tripleLine));
	}

	public boolean add(String publisher, Triple triple) {
		Map<String, String> record = getRecord(triple.subject());
		if(Objects.equals(record.get(triple.predicate()), triple.value())) return false;

		record.put(triple.predicate(), triple.value());
		updateRecord(triple.subject(), record);

		save(publisher, triple);
		return true;
	}

	protected void updateRecord(String id, Map<String, String> record) {
		masterMap.set(id, serializer().serialize(record));
	}

	protected Map<String, String> getRecord(String subject) {
		String serializedRecord = masterMap.get(subject);
		if(serializedRecord == null) return new HashMap<>();
		return serializer().deserialize(serializedRecord);
	}

	private synchronized void save(String publisher, String tripleLine) {
		save(publisher, new Triple(tripleLine));
	}

	protected synchronized void save(String publisher, Triple triple) {
		try(TriplesFileWriter writer = new TriplesFileWriter(config.dataDirectory(), publisher)) {
			writer.write(triple);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public MasterSerializer serializer() {
		return config.serializer();
	}

	public RecordTransformer transformer() {
		return config.transformer();
	}

	protected Config getHazelcastConfig() {
		Config hzConfig = new Config();
		hzConfig.setInstanceName(config.instanceName());
		hzConfig.setNetworkConfig(new NetworkConfig().setPort(config.port()));
		return hzConfig;
	}

	private void checkConfigValues() {
		if(config.instanceName() == null) throw new MasterInitializationException("Instance name cannot be null");
		if(config.dataDirectory() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.logDirectory() == null) throw new MasterInitializationException("Log directory cannot be null");
		if(config.port() <= 0) throw new MasterInitializationException("Port is invalid");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
	}

	private float getHazelcastMemoryUsedMB() {
		long metadata = metadataMap.getLocalMapStats().getOwnedEntryMemoryCost();
		long data = masterMap.getLocalMapStats().getOwnedEntryMemoryCost();
		return (metadata + data) / 1024.0f / 1024.0f;
	}
}