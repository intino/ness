package io.intino.ness.master.core;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.master.data.FileTripletLoader;
import io.intino.ness.master.data.MasterTripletsDigester;
import io.intino.ness.master.data.TripletLoader;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;
import io.intino.ness.master.serialization.MasterSerializers;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class Master {

	public static final String NONE_TYPE = "";

	private final Config config;
	private Map<String, String> masterMap;

	public Master(Config config) {
		this.config = requireNonNull(config);
		checkConfigValues();
	}

	public Map<String, String> masterMap() {
		return masterMap;
	}

	public File datalakeRootPath() {
		return config.datalakeRootPath();
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
			initMaps(data);
		}
		System.gc();
		Logger.trace("Data loaded into Master:\n" + histogram());
		Logger.info("Master initialized. Using " + getMemoryUsedMB() + " MB");
	}

	public void stop() {
		masterMap = new ConcurrentHashMap<>(0);
		System.gc();
	}

	protected void initMaps(Map<String, TripletRecord> data) {
		MasterSerializer serializer = serializer();
		masterMap = new ConcurrentHashMap<>(data.size());
		masterMap.putAll(data.entrySet().stream()
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

	public MasterSerializer serializer() {
		return config.serializer();
	}

	private void checkConfigValues() {
		if(config.datalakeRootPath() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
		if(config.tripletsDigester() == null) throw new MasterInitializationException("Triplet digester cannot be null");
		if(config.tripletLoader() == null) throw new MasterInitializationException("Triplet loader cannot be null");
	}

	private float getMemoryUsedMB() {
		System.gc();
		Runtime r = Runtime.getRuntime();
		long memory = r.totalMemory() - r.freeMemory();
		return memory / 1024.0f / 1024.0f;
	}

	private String histogram() {
		Map<String, Integer> histogram = new HashMap<>();
		masterMap.keySet().stream().map(Triplet::typeOf).map(t -> "\"" + t + "\"").forEach(key -> histogram.compute(key, (k, v) -> v == null ? 1 : v + 1));
		return "  " + histogram.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n  "));
	}

	public static class Config {

		private File datalakeRootPath;
		private MasterSerializer serializer = MasterSerializers.getDefault();
		private MasterTripletsDigester tripletsDigester = MasterTripletsDigester.createDefault();
		private TripletLoader tripletLoader;

		public Config() {
		}

		public Config(Map<String, String> arguments) {
			this.datalakeRootPath = new File(arguments.get("datalake_path"));
			this.serializer = MasterSerializers.get(arguments.getOrDefault("serializer", MasterSerializers.Standard.getDefault()));
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
	}
}