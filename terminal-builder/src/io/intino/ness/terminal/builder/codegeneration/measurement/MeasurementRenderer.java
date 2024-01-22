package io.intino.ness.terminal.builder.codegeneration.measurement;

import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Sensor.Magnitude;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.terminal.builder.codegeneration.Commons;
import io.intino.ness.terminal.builder.codegeneration.Formatters;

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
				.add("id", magnitude.id())
				.add("name", magnitude.name$())
				.add("owner", measurement);
		if (!magnitude.attributeList().isEmpty()) fb.add("attribute", serialize(magnitude.attributeList()));
		return fb.toFrame();
	}

	private static Frame[] serialize(List<Magnitude.Attribute> attributes) {
		return attributes.stream()
				.map(a -> new FrameBuilder().add("name", a.name$()).add("value", a.value()).toFrame())
				.toArray(Frame[]::new);
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
