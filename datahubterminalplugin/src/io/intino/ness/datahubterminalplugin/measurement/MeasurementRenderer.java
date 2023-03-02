package io.intino.ness.datahubterminalplugin.measurement;

import io.intino.datahub.model.Measurement;
import io.intino.datahub.model.Namespace;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;

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

	private Frame createMeasurementFrame(Measurement event, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("measurement")
				.add("name", event.name$())
				.add("package", packageName);
		Frame[] values = event.valueList().stream().map(m -> new FrameBuilder("value").add("name", m).add("owner", event.name$())).map(FrameBuilder::toFrame).toArray(Frame[]::new);
		eventFrame.add("value", values);
		return eventFrame.toFrame();
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
