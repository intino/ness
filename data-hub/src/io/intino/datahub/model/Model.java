package io.intino.datahub.model;

public class Model {

	public static String qn(Namespace self) {
		String prefix = self.core$().owner().is(Namespace.class) && !self.core$().ownerAs(Namespace.class).qn().isEmpty() ? self.core$().ownerAs(Namespace.class).qn() + "." : "";
		return prefix + self.name$();
	}

	public static String qn(Datalake.Tank self) {
		String namespace = namespace(self);
		String eventName = nameOf(self);
		return (namespace.isEmpty() ? "" : namespace + ".") + eventName;
	}

	private static String nameOf(Datalake.Tank self) {
		if(self.isMessage()) return self.asMessage().message.name$();
		if(self.isMeasurement()) return self.asMeasurement().sensor.name$();
		if(self.isResource()) return self.asResource().resourceEvent.name$();
		throw new IllegalArgumentException("Unknown tank type of " + self.name$());
	}

	private static String namespace(Datalake.Tank self) {
		if(self.isMessage()) return eventNamespace(self.asMessage().message);
		if(self.isMeasurement()) return eventNamespace(self.asMeasurement().sensor);
		if(self.isResource()) return eventNamespace(self.asResource().resourceEvent);
		throw new IllegalArgumentException("Unknown tank type of " + self.name$());
	}

	private static String eventNamespace(Resource resource) {
		return resource.core$().owner().is(Namespace.class) ? resource.core$().ownerAs(Namespace.class).qn() : "";
	}

	private static String eventNamespace(Message message) {
		return message.core$().owner().is(Namespace.class) ? message.core$().ownerAs(Namespace.class).qn() : "";
	}

	private static String eventNamespace(Sensor sensor) {
		return sensor.core$().owner().is(Namespace.class) ? sensor.core$().ownerAs(Namespace.class).qn() : "";
	}
}
