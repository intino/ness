package io.intino.datahub.model;

public class Model {

	public static String qn(Namespace self) {
		String prefix = self.core$().owner().is(Namespace.class) && !self.core$().ownerAs(Namespace.class).qn().isEmpty() ? self.core$().ownerAs(Namespace.class).qn() + "." : "";
		return prefix + self.name$();
	}

	public static String qn(Datalake.Tank self) {
		String namespace = namespace(self);
		String eventName = self.isMeasurement() ? self.asMeasurement().measurement.name$() : self.asMessage().message.name$();
		return (namespace.isEmpty() ? "" : namespace + ".") + eventName;
	}

	private static String namespace(Datalake.Tank self) {
		return self.isMeasurement() ? eventNamespace(self.asMeasurement().measurement) : eventNamespace(self.asMessage().message);
	}


	private static String eventNamespace(Message message) {
		return message.core$().owner().is(Namespace.class) ? message.core$().ownerAs(Namespace.class).qn() : "";
	}

	private static String eventNamespace(Measurement measurement) {
		return measurement.core$().owner().is(Namespace.class) ? measurement.core$().ownerAs(Namespace.class).qn() : "";
	}
}
