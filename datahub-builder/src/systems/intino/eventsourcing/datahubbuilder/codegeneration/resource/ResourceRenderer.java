package systems.intino.eventsourcing.datahubbuilder.codegeneration.resource;

import io.intino.itrules.Engine;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import systems.intino.eventsourcing.datahub.model.Namespace;
import systems.intino.eventsourcing.datahub.model.Resource;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Commons;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters;

import java.io.File;

public class ResourceRenderer {
	private static final String EVENT = "systems.intino.eventsourcing.event.resource.ResourceEvent";

	private final Resource resource;
	private final File destination;
	private final String rootPackage;

	public ResourceRenderer(Resource resource, File destination, String rootPackage) {
		this.resource = resource;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = resourcesPackage();
		if (resource.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + resource.core$().ownerAs(Namespace.class).qn();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createEventFrame(resource, rootPackage);
		Commons.writeFile(packageFolder, resource.name$(), engine().render(new FrameBuilder("root")
				.add("root", rootPackage)
				.add("package", rootPackage)
				.add("event", frame)));
	}

	private Frame createEventFrame(Resource event, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("event").
				add("name", event.name$()).add("package", packageName).
				add("parent", parent(event));
		return eventFrame.toFrame();
	}

	private String parent(Resource resource) {
		return EVENT;
	}

	private String resourcesPackage() {
		return rootPackage + ".resources";
	}

	private Engine engine() {
		return Formatters.customize(new Engine(new ResourceTemplate())).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
