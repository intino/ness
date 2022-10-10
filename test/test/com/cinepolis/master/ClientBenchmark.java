//package com.cinepolis.master;
//
//import com.cinepolis.master.model.CachedMasterClient;
//import com.cinepolis.master.model.LazyMasterClient;
//import com.cinepolis.master.model.MasterClient;
//import io.intino.master.model.Entity;
//import io.intino.master.model.Triple;
//
//import java.util.List;
//import java.util.Random;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class ClientBenchmark {
//
//	private static final int ITERATIONS = 8;
//	private static long blackhole = new Random().nextLong();
//
//	public static void main(String[] args) {
//
//		test("Cached", () -> new CachedMasterClient(config(MasterClient.Type.Cached)));
//		test("Lazy", () -> new LazyMasterClient(config(MasterClient.Type.Lazy)));
//
//		System.out.println("\n\n\n");
//
//		test("Cached", () -> new CachedMasterClient(config(MasterClient.Type.Cached)));
//		test("Lazy", () -> new LazyMasterClient(config(MasterClient.Type.Lazy)));
//
//		System.out.println(blackhole);
//	}
//
//	private static void test(String name, Supplier<MasterClient> masterImpl) {
//		warmup(masterImpl);
//
//		long loadTime = 0;
//		long jvmMem = 0;
//		long queryTime = 0;
//		long publishTime = 0;
//
//		for(int i = 0;i < ITERATIONS;i++) {
//			gc();
//			long start = System.currentTimeMillis();
//
//			MasterClient master = masterImpl.get();
//			master.start();
//			blackhole += master.hashCode();
//
//			loadTime += System.currentTimeMillis() - start;
//			jvmMem += getJvmMemoryUsed();
//
//			String[] theaterIds = master.theaters().map(Entity::id).toArray(String[]::new);
//			String[] employeeIds = master.employees().map(Entity::id).toArray(String[]::new);
//
//			start = System.currentTimeMillis();
//
//			for(int j = 0;j < ITERATIONS;j++) {
//				for(String id : theaterIds) blackhole += master.theater(id).hashCode();
//				for(String id : employeeIds) blackhole += master.employee(id).hashCode();
//			}
//
//			queryTime += (System.currentTimeMillis() - start) / ITERATIONS;
//
//			List<Triple> triples = IntStream.range(0, 100)
//					.mapToObj(v -> new Triple((v % 10) + ":theater", String.valueOf(v), String.valueOf(v)))
//					.collect(Collectors.toList());
//
//			start = System.currentTimeMillis();
//
//			for(Triple t : triples) master.publish("test", t);
//
//			publishTime += System.currentTimeMillis() - start;
//
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//
//			master.stop();
//		}
//
//		float timeResult = loadTime / (float) ITERATIONS;
//		float queryTimeResult = queryTime / (float) ITERATIONS;
//		float publishTimeResult = publishTime / (float) ITERATIONS;
//		float jvmMemResult = jvmMem / (float) ITERATIONS / 1024.0f / 1024.0f;
//
//		System.out.println("name\tload-time-ms\tquery-time-ms\t\tpublish-time-ms\tjvm-mem-mb");
//		System.out.println(name + "\t" + String.format("%04f", timeResult)
//				+ "\t" + String.format("%04f", queryTimeResult)
//				+ "\t" + String.format("%04f", publishTimeResult)
//				+ "\t" + String.format("%04f", jvmMemResult));
//	}
//
//	private static void warmup(Supplier<MasterClient> masterImpl) {
//		gc();
//		MasterClient masterClient = masterImpl.get();
//		masterClient.start();
//		blackhole += masterClient.hashCode();
//		masterClient.stop();
//	}
//
//	private static long getJvmMemoryUsed() {
//		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//	}
//
//	private static void gc() {
//		System.gc();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private static MasterClient.Config config(MasterClient.Type type) {
//		return new MasterClient.Config()
//				.instanceName("Client-Test")
//				.addresses(List.of("localhost:62555"))
//				.type(type)
//				.readOnly(false);
//	}
//
//	private static class BenchmarkResult {
//		public String name;
//		public float loadTimeMs;
//		public float queryTimeMs;
//		public float jvmMemMb;
//	}
//}
