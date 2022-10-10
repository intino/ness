package io.intino.master;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class CreateTriplesData {

	private static final long START_ID = 100_000;
	private static final Random Rand = new Random(System.nanoTime());

	public static void main(String[] args) throws IOException {
//		generate(1_000_000,10,"temp/cinepolis-data/mock-1M-10/mock-1M-10.triples");
//		generate(100_000,10,"temp/cinepolis-data/mock-100K-10/mock-100K-10.triples");
//		generate(10_000,10, "temp/cinepolis-data/mock-10K-10/mock-10K-10.triples");
//		generate(1_000,10,"temp/cinepolis-data/mock-1K-10/mock-1K-10.triples");
//
//		generate(1_000_000,3,"temp/cinepolis-data/mock-1M-3/mock-1M-3.triples");
//		generate(100_000,3,"temp/cinepolis-data/mock-100K-3/mock-100K-3.triples");
//		generate(10_000,3, "temp/cinepolis-data/mock-10K-3/mock-10K-3.triples");
//		generate(1_000,3,"temp/cinepolis-data/mock-1K-3/mock-1K-3.triples");
//
//		generate(1_000_000,1,"temp/cinepolis-data/mock-1M-1/mock-1M-1.triples");
//		generate(100_000,1,"temp/cinepolis-data/mock-100K-1/mock-100K-1.triples");
//		generate(10_000,1, "temp/cinepolis-data/mock-10K-1/mock-10K-1.triples");
		generate(1_000,1,"temp/cinepolis-data/mock-1K-1/mock-1K-1.triples");

		generate(100, 1000,"temp/cinepolis-data/mock-100-1K/mock-100-1K.triples");
	}

	private static void generate(int numRecords, int numFields, String file) throws IOException {
		new File(file).getParentFile().mkdirs();
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			long id = START_ID;
			for(int j = 0; j < numRecords; j++) {
				addAttrib(writer, id, "name", "n" + id);
				if(numFields >= 2) addAttrib(writer, id, "age", randomAge());
				if(numFields >= 3) addAttrib(writer, id, "postalCode", randomPostalCode());
				if(numFields >= 4) addAttrib(writer, id, "phone", randomPhone());
				if(numFields >= 5) addAttrib(writer, id, "company", randomCompany());
				if(numFields >= 6) addAttrib(writer, id, "department", randomDepartment());
				if(numFields >= 7) addAttrib(writer, id, "country", randomCountry());
				if(numFields >= 8) addAttrib(writer, id, "address", randomAddress());
				if(numFields >= 9) addAttrib(writer, id, "area", randomArea());
				if(numFields >= 10) addAttrib(writer, id, "foreman", randomForeman());
				if(numFields >= 11) {
					for(int i = 11;i < numFields;i++) {
						addAttrib(writer, id, "attrib-" + i, "v-" + Rand.nextInt());
					}
				}
				++id;
			}
		}
	}

	private static Object randomAge() {
		return Rand.nextInt(100) + 18;
	}

	private static Object randomPostalCode() {
		return Rand.nextInt(100000) + 10000;
	}

	private static Object randomPhone() {
		return Math.abs(Rand.nextLong() + 1000000000L);
	}

	private static Object randomCompany() {
		return "Comp_" + Rand.nextInt() + (Rand.nextFloat() > 0.9f ? "S.L." : "");
	}

	private static Object randomDepartment() {
		return "Dep-" + Rand.nextInt(100);
	}

	private static final String[] Countries = {"Spain", "Mexico", "USA", "Germany", "England", "France", "Japan", "China"};
	private static Object randomCountry() {
		return Countries[Rand.nextInt(Countries.length)];
	}

	private static Object randomAddress() {
		return "C/" + Rand.nextInt() + ", " + Rand.nextFloat();
	}

	private static Object randomArea() {
		return randomCountry() + "." + Rand.nextInt(10) + 1;
	}

	private static Object randomForeman() {
		return Rand.nextInt((int) START_ID) + START_ID;
	}

	private static void addAttrib(BufferedWriter writer, long id, String attribName, Object value) throws IOException {
		writer.write(id + "\t" + attribName + "\t" + value);
		writer.newLine();
	}
}
