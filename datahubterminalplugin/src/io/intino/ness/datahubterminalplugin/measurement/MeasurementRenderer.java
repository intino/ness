package io.intino.ness.datahubterminalplugin.measurement;

import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Sensor.Magnitude;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeasurementRenderer {
	private final Sensor sensor;
	private final File destination;
	private final String rootPackage;

	public MeasurementRenderer(Sensor sensor, File destination, String rootPackage) {
		this.sensor = sensor;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = rootPackage();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createMeasurementFrame(sensor, rootPackage);
		Commons.writeFrame(packageFolder, sensor.name$(), template().render(frame));
	}

	private Frame createMeasurementFrame(Sensor sensor, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("measurement")
				.add("name", sensor.name$())
				.add("size", sensor.magnitudeList().size())
				.add("package", packageName);
		List<Frame> list = new ArrayList<>();
		List<Magnitude> magnitudes = sensor.magnitudeList();
		for (int i = 0; i < magnitudes.size(); i++)
			list.add(frame(sensor.name$(), magnitudes.get(i), i));
		eventFrame.add("value", list.toArray(new Frame[0]));
		return eventFrame.toFrame();
	}

	private static Frame frame(String measurement, Magnitude magnitude, int i) {
		FrameBuilder fb = new FrameBuilder("value")
				.add("index", i)
				.add("name", magnitude.id())
				.add("owner", measurement);
		if (!magnitude.attributeList().isEmpty()) fb.add("attribute", toString(magnitude.attributeList()));
		return fb.toFrame();
	}

	private static String[] toString(List<Magnitude.Attribute> attributes) {
		return attributes.stream()
				.map(a -> a.name$() + ":" + a.value())
				.toArray(String[]::new);
	}

	private String rootPackage() {
		String rootPackage = measurementsPackage();
		if (sensor.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + sensor.core$().ownerAs(Namespace.class).qn();
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
