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
	private static final String EVENT = "io.intino.alexandria.event.MeasurementEvent";
	private final Measurement event;
	private final File destination;
	private final String rootPackage;

	public MeasurementRenderer(Measurement measurement, File destination, String rootPackage) {
		this.event = measurement;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = eventsPackage();
		if (event.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + event.core$().ownerAs(Namespace.class).qn();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createMeasurementFrame(event, rootPackage);
		Commons.writeFrame(packageFolder, event.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("event", frame)));
	}

	private Frame createMeasurementFrame(Measurement event, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("event").
				add("name", event.name$()).add("package", packageName);
		Frame[] values = event.valueList().stream().map(m -> new FrameBuilder("value").add("name", m).add("owner", event.name$())).map(FrameBuilder::toFrame).toArray(Frame[]::new);
		eventFrame.add("value", values);
		return eventFrame.toFrame();
	}

	private String eventsPackage() {
		return rootPackage + ".events";
	}

	private Template template() {
		return Formatters.customize(new MeasurementTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
