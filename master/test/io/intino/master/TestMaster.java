//package io.intino.master;
//
//import com.google.gson.Gson;
//import com.hazelcast.config.Config;
//import com.hazelcast.config.NetworkConfig;
//import com.hazelcast.core.Hazelcast;
//import com.hazelcast.internal.util.MutableInteger;
//import com.hazelcast.map.IMap;
//import io.intino.alexandria.logger.Logger;
//import io.intino.master.core.Launcher;
//import io.intino.master.core.Master;
//import io.intino.master.model.Triple;
//import org.xerial.snappy.Snappy;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static io.intino.master.model.Triple.TRIPLE_SEPARATOR;
//import static java.util.stream.Collectors.groupingBy;
//import static java.util.stream.Collectors.toMap;
//
//public class TestMaster extends Master {
//
//	public static void main(String[] args) {
//		// Warmup
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1K-1"});
//
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1K-1"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1K-3"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1K-10"});
//
//		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/datasets"});
//
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-100-1K"});
////
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-10K-1"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-10K-3"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-10K-10"});
////
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-100K-1"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-100K-3"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-100K-10"});
////
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1M-1"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1M-3"});
////		new Launcher().setMasterImpl(TestMaster::new).launch(new String[] {"triples_folder=temp/cinepolis-data/mock-1M-10"});
//
//		System.out.println("====> DONE");
//	}
//
//	private final Set<String> types = new HashSet<>();
//
//	public TestMaster(File folder) {
//		super(folder);
//	}
//
//	@Override
//	public void start() {
//		sleep(3000);
//
//		Config config = new Config();
//		config.setInstanceName("master");
//		config.setNetworkConfig(new NetworkConfig().setPort(62555).setPortAutoIncrement(false));
//		hazelcast = Hazelcast.getOrCreateHazelcastInstance(config);
//
//		initMaps();
//
//		List<LoadStats> stats = new ArrayList<>();
//
////		stats.add(loadData());
////		clear();
////
////		stats.add(loadData2());
////		clear();
////
////		stats.add(loadData3());
////		clear();
//
//		stats.add(loadData4());
////		clear();
////
////		stats.add(loadData5());
////		clear();
//
////		stats.add(loadData6());
////		clear();
//
////		writeStats(stats);
//
//		setupListeners();
//
////		hz.shutdown();
////		System.gc();
////		sleep(1000);
//	}
//
//	// Name	num-entries	total-mem-MB	load-time-ms
//	private void writeStats(List<LoadStats> stats) {
//		File file = new File(rootDir, "stats-" + rootDir.getName() + ".tsv");
//		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//			writeAbsoluteStats(stats, writer);
//			writeRelativeStats(stats, writer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			System.out.println(Files.readString(file.toPath()));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void writeRelativeStats(List<LoadStats> stats, BufferedWriter writer) throws IOException {
//		writer.newLine();
//		writer.write("name\tnum-entries-%\thz-mem-%\tjvm-mem-mb\ttotal-mem-%\tload-time-%");
//		writer.newLine();
//
//		int maxNumEntries = stats.stream().mapToInt(s -> s.numEntries).max().getAsInt();
//		float maxMemMB = (float) stats.stream().mapToDouble(s -> s.hzTotalMemMB).max().getAsDouble();
//		float jvmMaxMem = (float) stats.stream().mapToDouble(s -> s.jvmTotalMemMB).max().getAsDouble();
//		float maxTotalMem = (float) stats.stream().mapToDouble(s -> s.totalMemoryMB()).max().getAsDouble();
//		long maxLoadTime = stats.stream().mapToLong(s -> s.loadTimeMs).max().getAsLong();
//
//		for(LoadStats s : stats) {
//			float numEntries = s.numEntries / (float) maxNumEntries * 100;
//			float hzMem = s.hzTotalMemMB / maxMemMB * 100;
//			float jvmMem = s.jvmTotalMemMB / jvmMaxMem * 100;
//			float totalMem = s.totalMemoryMB() / maxTotalMem * 100;
//			float loadTimeMs = s.loadTimeMs / (float) maxLoadTime * 100;
//
//			writer.write(s.name
//					+ "\t" + String.format("%.4f", numEntries)
//					+ "\t" + String.format("%.4f", hzMem)
//					+ "\t" + String.format("%.4f", jvmMem)
//					+ "\t" + String.format("%.4f", totalMem)
//					+ "\t" + String.format("%.4f", loadTimeMs));
//			writer.newLine();
//		}
//	}
//
//	private static void writeAbsoluteStats(List<LoadStats> stats, BufferedWriter writer) throws IOException {
//		writer.write("name\tnum-entries\thz-mem-MB\tjvm-mem-mb\ttotal-mem-mb\tload-time-ms");
//		writer.newLine();
//		for(LoadStats s : stats) {
//			writer.write(s.name
//					+ "\t" + s.numEntries
//					+ "\t" + String.format("%.2f", s.hzTotalMemMB)
//					+ "\t" +  String.format("%.2f", s.jvmTotalMemMB)
//					+ "\t" +  String.format("%.2f", s.totalMemoryMB())
//					+ "\t" + s.loadTimeMs);
//			writer.newLine();
//		}
//	}
//
//	private LoadStats loadData6() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (6)");
//
//		MutableInteger count = new MutableInteger();
//
//		{
//			Map<String, Map<String, String>> records = new HashMap<>();
//
//			readTriplesIn(rootDir)
//					.map(t -> new Triple(t.subject(), t.predicate(), t.value()))
//					.forEach(t -> {
//						Map<String, String> record = records.computeIfAbsent(t.subject(), k -> new HashMap<>());
////						record.put("id", t.subject());
//						record.put(t.predicate(), t.value());
//					});
//
//			Gson gson = new Gson();
//
//			var recordsPerType = records.entrySet().stream().collect(groupingBy(e -> typeOf(e.getKey())));
//
//			for(var entry : recordsPerType.entrySet()) {
//				String type = entry.getKey();
//				types.add(type);
//				IMap<String, String> map = hazelcast.getMap(type);
//				map.setAll(entry.getValue().stream().collect(Collectors.toMap(Map.Entry::getKey, r -> gson.toJson(r.getValue()))));
//				count.value += map.size();
//			}
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		Logger.info("Num types: " + types.size());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store records (json) (scatter)";
//		stats.numEntries = count.value;
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		System.gc();
//		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//		System.out.println("JVM mem: " + usedMemory / 1024.0 / 1024.0 + " MB");
//
//		return stats;
//	}
//
//	private String typeOf(String id) {
//		return id.contains(":") ? id.substring(id.indexOf(':') + 1) : "unknown";
//	}
//
//	private LoadStats loadData5() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (5)");
//
//		{
//			Map<String, Map<String, String>> records = new HashMap<>();
//
//			readTriplesIn(rootDir)
//					.map(t -> new Triple(t.subject(), t.predicate(), t.value()))
//					.forEach(t -> records.computeIfAbsent(t.subject(), k -> new HashMap<>()).put(t.predicate(), t.value()));
//
//			Gson gson = new Gson();
//
//			master.setAll(records.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> compress(gson.toJson(e.getValue())))));
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store records (json + snappy)";
//		stats.numEntries = master.size();
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		return stats;
//	}
//
//	private float getJvmMemUsed() {
//		System.gc();
//		sleep(1000);
//		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0f / 1024.0f;
//	}
//
//	private void sleep(int i) {
//		try {
//			Thread.sleep(i);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private LoadStats loadData4() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (4)");
//
//		{
//			Map<String, Map<String, String>> records = new HashMap<>();
//
//			readTriplesIn(rootDir)
//					.map(t -> new Triple(t.subject(), t.predicate(), t.value()))
//					.forEach(t -> records.computeIfAbsent(t.subject(), k -> new HashMap<>()).put(t.predicate(), t.value()));
//
//			Gson gson = new Gson();
//
//			master.setAll(records.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> gson.toJson(e.getValue()))));
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store records (json)";
//		stats.numEntries = master.size();
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		System.gc();
//		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//
//		System.out.println("JVM mem: " + usedMemory / 1024.0 / 1024.0 + " MB");
//
//		return stats;
//	}
//
//	private LoadStats loadData3() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (3)");
//
//		{
//			Map<String, Map<String, String>> records = new HashMap<>();
//
//			readTriplesIn(rootDir)
//					.map(t -> new Triple(t.subject(), t.predicate(), t.value()))
//					.forEach(t -> records.computeIfAbsent(t.subject(), k -> new HashMap<>()).put(t.predicate(), t.value()));
//
//			master.setAll(records);
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store records (HashMap)";
//		stats.numEntries = master.size();
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		return stats;
//	}
//
//	private final Base64.Encoder base64 = Base64.getEncoder();
//	private byte[] compress(String s) {
//		try {
//			return Snappy.compress(s);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private LoadStats loadData2() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (2)");
//
//		{
//			master.setAll(readTriplesIn(rootDir)
//					.map(t -> new Triple(t.subject(), t.predicate(), t.value()))
//					.collect(toMap(t -> t.subject() + TRIPLE_SEPARATOR + t.predicate(), Triple::value, (k1, k2) -> k1)));
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store Triples";
//		stats.numEntries = master.size();
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		return stats;
//	}
//
//	private void initMaps() {
//		master = hazelcast.getMap("master");
//		subjectFactors = hazelcast.getMap("subjects");
//		predicateFactors = hazelcast.getMap("predicates");
//		reverseSubjectFactors = hazelcast.getMap("reverseSubjectFactors");
//		reversePredicateFactors = hazelcast.getMap("reversePredicateFactors");
//	}
//
//	private LoadStats loadData() {
//		System.gc();
//		sleep(500);
//
//		long start = System.currentTimeMillis();
//		Logger.info("Loading data (1)");
//
//		{
//			master.setAll(readTriplesIn(rootDir)
//					.map(t -> new Triple(
//							subjectFactor(t.subject()),
//							predicateFactor(t.predicate()),
//							value(t.value())
//					))
//					.collect(toMap(t -> t.subject() + TRIPLE_SEPARATOR + t.predicate(), Triple::value, (k1, k2) -> k1)));
//
//			reverseSubjectFactors.setAll(subjectFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
//
//			reversePredicateFactors.setAll(predicateFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
//		}
//
//		long time = System.currentTimeMillis() - start;
//		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
//
//		LoadStats stats = new LoadStats();
//		stats.name = "Store Triples + factors strategy";
//		stats.numEntries = master.size();
//		stats.hzTotalMemMB = getHzMemoryUsed();
//		stats.jvmTotalMemMB = getJvmMemUsed();
//		stats.loadTimeMs = time;
//
//		return stats;
//	}
//
//	private float getHzMemoryUsed() {
//		long valuesMem = master.getLocalMapStats().getOwnedEntryMemoryCost();
//		long subjectsMem = subjectFactors == null ? 0 : subjectFactors.getLocalMapStats().getOwnedEntryMemoryCost();
//		long predicatesMem = predicateFactors == null ? 0 : predicateFactors.getLocalMapStats().getOwnedEntryMemoryCost();
//		long reverseSubjectsMem = reverseSubjectFactors == null ? 0 : reverseSubjectFactors.getLocalMapStats().getOwnedEntryMemoryCost();
//		long reversePredicatesMem = reversePredicateFactors == null ? 0 : reversePredicateFactors.getLocalMapStats().getOwnedEntryMemoryCost();
//
//		long typeMapsMem = types.stream().mapToLong(t -> hazelcast.getMap(t).getLocalMapStats().getOwnedEntryMemoryCost()).sum();
//
//		return (valuesMem + subjectsMem + predicatesMem + reverseSubjectsMem + reversePredicatesMem + typeMapsMem) / 1024.0f / 1024.0f;
//	}
//
//	public void clear() {
//		master.clear();
//		if(subjectFactors != null) subjectFactors.clear();
//		if(predicateFactors != null) predicateFactors.clear();
//		if(reverseSubjectFactors != null) reverseSubjectFactors.clear();
//		if(reversePredicateFactors != null) reversePredicateFactors.clear();
//		types.forEach(t -> hazelcast.getMap(t).clear());
//		types.clear();
//		System.gc();
//	}
//
//	public static class LoadStats {
//		public String name;
//		public int numEntries;
//		public float hzTotalMemMB;
//		public float jvmTotalMemMB;
//		public long loadTimeMs;
//
//		public float totalMemoryMB() {
//			return hzTotalMemMB + jvmTotalMemMB;
//		}
//	}
//}
