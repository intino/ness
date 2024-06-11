package io.intino.ness.builder.codegeneration.resource;

import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Resource;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.builder.codegeneration.Commons;
import io.intino.ness.builder.codegeneration.Formatters;

import java.io.File;

public class ResourceRenderer {
	private static final String EVENT = "io.intino.alexandria.event.resource.ResourceEvent";

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
		Commons.writeFrame(packageFolder, resource.name$(), template().render(new FrameBuilder("root")
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

	private Template template() {
		return Formatters.customize(new ResourceTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
