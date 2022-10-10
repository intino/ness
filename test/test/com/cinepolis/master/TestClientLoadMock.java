package com.cinepolis.master;


import com.google.gson.Gson;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestClientLoadMock {

	private static final int Iterations = 5;

	private static HazelcastInstance hz;
	private static Map<String, Mock> mocks = new HashMap<>();

	public static void main(String[] args) {
		ClientConfig config = new ClientConfig();
		config.getNetworkConfig().addAddress("localhost:5701");
		hz = HazelcastClient.newHazelcastClient(config);

		loadData4();
	}

	private static void loadData1() {
		long time = 0;

		for(int i = 0;i < Iterations;i++) {
			mocks.clear();
			System.gc();

			long start = System.currentTimeMillis();

			Map<String, String> reverseSubjectFactors = hz.getMap("reverseSubjectFactors");
			Map<String, String> reversePredicateFactors = hz.getMap("reversePredicateFactors");

			hz.<String, String>getMap("master").forEach((k, v) -> {

				String[] subjectPredicate = k.split("\t");

				String subject = reverseSubjectFactors.get(subjectPredicate[0]);
				String predicate = reversePredicateFactors.get(subjectPredicate[1]);

				Mock mock = mocks.computeIfAbsent(subject, key -> new Mock());
				mock.id = subject;

				switch (predicate) {
					case "name":
						mock.name = v;
						break;
					case "age":
						mock.age = v;
						break;
					case "postalCode":
						mock.postalCode = v;
						break;
					case "phone":
						mock.phone = v;
						break;
					case "company":
						mock.company = v;
						break;
					case "department":
						mock.department = v;
						break;
					case "country":
						mock.country = v;
						break;
					case "address":
						mock.address = v;
						break;
					case "area":
						mock.area = v;
						break;
					case "foreman":
						mock.foreman = v;
						break;
				}

				mocks.put(mock.id, mock);
			});

			time += System.currentTimeMillis() - start;
		}

		time /= Iterations;

		System.out.println("Data loaded (1) in " + time + " ms");
	}

	private static void loadData2() {
		long time = 0;

		for(int i = 0;i < Iterations;i++) {
			mocks.clear();
			System.gc();

			long start = System.currentTimeMillis();

			hz.<String, String>getMap("master").forEach((k, v) -> {

				String[] subjectPredicate = k.split("\t");

				String subject = subjectPredicate[0];
				String predicate = subjectPredicate[1];

				Mock mock = mocks.computeIfAbsent(subject, key -> new Mock());
				mock.id = subject;

				switch (predicate) {
					case "name":
						mock.name = v;
						break;
					case "age":
						mock.age = v;
						break;
					case "postalCode":
						mock.postalCode = v;
						break;
					case "phone":
						mock.phone = v;
						break;
					case "company":
						mock.company = v;
						break;
					case "department":
						mock.department = v;
						break;
					case "country":
						mock.country = v;
						break;
					case "address":
						mock.address = v;
						break;
					case "area":
						mock.area = v;
						break;
					case "foreman":
						mock.foreman = v;
						break;
				}

				mocks.put(mock.id, mock);
			});

			time += System.currentTimeMillis() - start;
		}

		time /= Iterations;

		System.out.println("Data loaded (2) in " + time + " ms");
	}

	private static void loadData3() {

		long time = 0;

		for(int i = 0;i < Iterations;i++) {
			mocks.clear();
			System.gc();

			long start = System.currentTimeMillis();

			hz.<String, Map<String, String>>getMap("master").forEach((k, v) -> {

				Mock mock = new Mock();
				mock.id = k;
				mock.name = v.get("name");
				mock.age = v.get("age");
				mock.postalCode = v.get("postalCode");
				mock.phone = v.get("phone");
				mock.company = v.get("company");
				mock.department = v.get("department");
				mock.country = v.get("country");
				mock.address = v.get("address");
				mock.area = v.get("area");
				mock.foreman = v.get("foreman");

				mocks.put(mock.id, mock);
			});

			time += System.currentTimeMillis() - start;
		}

		time /= Iterations;

		System.out.println("Data loaded (3) in " + time + " ms");
	}

	private static void loadData4() {
		Gson gson = new Gson();

		long time = 0;

		for(int i = 0;i < Iterations;i++) {
			mocks.clear();
			System.gc();

			long start = System.currentTimeMillis();

			hz.<String,String>getMap("master").forEach((k, v) -> {

				Map<String, String> record = gson.fromJson(v, Map.class);

				Mock mock = new Mock();
				mock.id = k;
				mock.name = record.get("name");
				mock.age = record.get("age");
				mock.postalCode = record.get("postalCode");
				mock.phone = record.get("phone");
				mock.company = record.get("company");
				mock.department = record.get("department");
				mock.country = record.get("country");
				mock.address = record.get("address");
				mock.area = record.get("area");
				mock.foreman = record.get("foreman");

				mocks.put(mock.id, mock);
			});

			time += System.currentTimeMillis() - start;
		}

		time /= Iterations;

		System.out.println("Data loaded (4) in " + time + " ms");
	}

	private static void loadData5() {
		Gson gson = new Gson();

		long time = 0;

		for(int i = 0;i < Iterations;i++) {
			mocks.clear();
			System.gc();

			long start = System.currentTimeMillis();

			hz.<String, byte[]>getMap("master").forEach((k, v) -> {

				String uncompress = uncompress(v);
				Map<String, String> record = gson.fromJson(uncompress, Map.class);

				Mock mock = new Mock();
				mock.id = k;
				mock.name = record.get("name");
				mock.age = record.get("age");
				mock.postalCode = record.get("postalCode");
				mock.phone = record.get("phone");
				mock.company = record.get("company");
				mock.department = record.get("department");
				mock.country = record.get("country");
				mock.address = record.get("address");
				mock.area = record.get("area");
				mock.foreman = record.get("foreman");

				mocks.put(mock.id, mock);
			});

			time += System.currentTimeMillis() - start;
		}

		time /= Iterations;

		System.out.println("Data loaded (5) in " + time + " ms");
	}

	private static String uncompress(byte[] v) {
		try {
			return Snappy.uncompressString(v);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Mock {

		public String id;
		public String name;
		public String age;
		public String postalCode;
		public String phone;
		public String company;
		public String department;
		public String country;
		public String address;
		public String area;
		public String foreman;
	}
}
