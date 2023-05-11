package org.example.test.chronos;

import io.intino.alexandria.Json;
import io.intino.alexandria.zit.model.Period;
import io.intino.sumus.chronos.TimelineFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class AnalyzeTimelineFile {

	public static void main(String[] args) throws IOException {
		analyze("C:\\Users\\naits\\Downloads\\EC2AMAZ-D67CFU1.timeline");
	}

	private static void analyze(String path) throws IOException {
//		TimelineFile.open(new File(path)).export(new File(path + ".itl"));

		try(DataInputStream reader = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
			Header header = new Header(ByteBuffer.wrap(reader.readNBytes(512)));
			System.out.println(header);

			int sensorModelCount = 0;
			int timeModelCount = 0;
			int dataBlockCount = 0;

			short lastMark = 0;
			int count = 0;

			while(reader.available() > 0) {
				short mark = reader.readShort();
				switch (mark) {
					case 0x5555 -> {
						dataBlockCount++;
						int size = reader.readInt();
						reader.skipBytes(size);
						if(lastMark != mark) {
							System.out.println("DataBlock (" + size + " bytes) x" + count);
							count = 1;
						}
						else count++;
						lastMark = mark;
					}
					case 0x6660 -> {
						timeModelCount++;
						Instant instant = Instant.ofEpochMilli(reader.readLong());
						Period period = Period.of(reader.readShort(), reader.readShort());
						if(lastMark != mark) {
							System.out.println("\nTimeModel (" + instant + ", " + period + ")");
							count = 1;
						}
						else count++;
						lastMark = mark;
					}
					case 0x6661 -> {
						sensorModelCount++;
						String measurements = new String(reader.readNBytes(reader.readInt())).replace("\n", ", ");
						if(lastMark != mark) {
							System.out.println("SensorModel: " + measurements);
							count = 1;
						}
						else count++;
						lastMark = mark;
					}
				}
			}

			System.out.println();
			System.out.println("TimeModel count = " + timeModelCount);
			System.out.println("SensorModel count = " + sensorModelCount);
			System.out.println("Data blocks count = " + dataBlockCount);
		}
	}

	private static class Header {

		long recordCount;
        long sensorModelPosition;
        long timeModelPosition;
        Instant first;
		Instant last;
		Instant next;
        boolean compressed;
        String sensor;

		public Header(ByteBuffer buffer) throws IOException {
			assertEquals(0x5005, buffer.getShort());

			this.recordCount = buffer.getLong();
			this.sensorModelPosition = buffer.getLong();
			this.timeModelPosition = buffer.getLong();
			this.first = Instant.ofEpochMilli(buffer.getLong());
			this.last = Instant.ofEpochMilli(buffer.getLong());
			this.next = Instant.ofEpochMilli(buffer.getLong());
			this.compressed = buffer.get() == 1;
			this.sensor = new DataInputStream(new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.capacity())).readUTF();
		}

		@Override
		public String toString() {
			return Json.toJsonPretty(this);
		}
	}
}
