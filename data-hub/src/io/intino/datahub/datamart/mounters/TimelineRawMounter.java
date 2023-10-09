package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.timelines.TimelineWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

import static io.intino.datahub.datamart.mounters.TimelineUtils.getOrCreateTimelineStoreOfRawTimeline;
import static io.intino.datahub.datamart.mounters.TimelineUtils.sourceSensor;

public class TimelineRawMounter {
	private final DataHubBox box;
	private final MasterDatamart datamart;

	public TimelineRawMounter(DataHubBox box, MasterDatamart datamart) {
		this.box = box;
		this.datamart = datamart;
	}

	public void mount(MeasurementEvent event) {
		try {
			if (event.ss() == null) return;
			TimelineStore store = getOrCreate(event, sourceSensor(event));
			update(store, event);
		} catch (Exception e) {
			Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
		}
	}

	private TimelineStore getOrCreate(MeasurementEvent event, String sensor) throws IOException {
		TimelineStore store = datamart.timelineStore().get(event.type(), sensor);
		if (store == null)
			store = getOrCreateTimelineStoreOfRawTimeline(box.datamartTimelinesDirectory(datamart.name()), datamart, event.ts(), event.type(), sensor);
		return store;
	}

	protected void update(TimelineStore tlStore, MeasurementEvent event) throws IOException {
		try(TimelineWriter writer = tlStore.writer()) {
			checkTs(event.ts(), writer);
			writer.set(event.values()); // TODO: measurements must be present in sensorModel and in the order defined by the sensorModel
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static double[] measurementsOf(MeasurementEvent event, TimelineStore tlStore) {
		TimelineStore.SensorModel sensorModel = tlStore.sensorModel();
		double[] measurements = new double[sensorModel.size()];
		Arrays.fill(measurements, Double.NaN);
		for(int i = 0; i < event.magnitudes().length;i++) {
			int index = sensorModel.indexOf(name(event, i));
			double value = index >= 0 ? event.values()[i] : Double.NaN;
			measurements[index] = value;
		}
		return measurements;
	}

	private static void checkTs(Instant ts, TimelineWriter writer) throws IOException {
		long lapse = Duration.between(writer.header().next(), ts).getSeconds();
		if (lapse > writer.timeModel().period().duration() * 2) writer.set(ts);
	}

	private static String name(MeasurementEvent event, int i) {
		//FIXME remove when all measurement events removed
		String name = event.magnitudes()[i].name();
		return name.contains("=") ? name.substring(0, name.indexOf(":")) : name;
	}

	public static class OfSingleTimeline extends TimelineRawMounter {

		private final Supplier<TimelineWriter> writer;

		public OfSingleTimeline(MasterDatamart datamart, Supplier<TimelineWriter> writer) {
			super(datamart.box(), datamart);
			this.writer = writer;
		}

		@Override
		public void mount(MeasurementEvent event) {
			try {
				TimelineWriter writer = this.writer.get();
				checkTs(event.ts(), writer);
				writer.set(event.values()); // TODO: measurements must be present in sensorModel and in the order defined by the sensorModel
			} catch (Exception e) {
				Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
			}
		}
	}
}
