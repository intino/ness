package io.intino.ness.datahubterminalplugin.measurement;

import io.intino.datahub.model.Measurement;
import io.intino.datahub.model.Namespace;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeasurementRenderer {
	private final Measurement measurement;
	private final File destination;
	private final String rootPackage;

	public MeasurementRenderer(Measurement measurement, File destination, String rootPackage) {
		this.measurement = measurement;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = rootPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createMeasurementFrame(measurement, rootPackage);
		Commons.writeFrame(packageFolder, measurement.name$(), template().render(frame));
	}

	private Frame createMeasurementFrame(Measurement measurement, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("measurement")
				.add("name", measurement.name$())
				.add("size", measurement.valueList().size())
				.add("package", packageName);
		List<Frame> list = new ArrayList<>();
		List<Measurement.Value> valueList = measurement.valueList();
		for (int i = 0, valueListSize = valueList.size(); i < valueListSize; i++) {
			Measurement.Value m = valueList.get(i);
			Frame frame = frame(measurement.name$(), m, i);
			list.add(frame);
		}
		Frame[] object = list.toArray(new Frame[0]);
		eventFrame.add("value", object);
		return eventFrame.toFrame();
	}

	private static Frame frame(String measurement, Measurement.Value v, int i) {
		return new FrameBuilder("value")
				.add("index", i)
				.add("name", v.name$())
				.add("owner", measurement)
				.toFrame();
	}

	private String rootPackage() {
		String rootPackage = measurementsPackage();
		if (measurement.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + measurement.core$().ownerAs(Namespace.class).qn();
		return rootPackage;
	}

	private String measurementsPackage() {
		return rootPackage + ".measurements";
	}

	private Template template() {
		return Formatters.customize(new MeasurementTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
