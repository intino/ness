package io.intino.datahub.model;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class Model {

	public static String qn(Datalake.Split self) {
		String prefix = self.core$().owner().is(Datalake.Split.class) && !self.core$().ownerAs(Datalake.Split.class).qn().isEmpty() ? self.core$().ownerAs(Datalake.Split.class).qn() + "." : "";
		return prefix + self.label();
	}

	public static String qn(Namespace self) {
		String prefix = self.core$().owner().is(Namespace.class) && !self.core$().ownerAs(Namespace.class).qn().isEmpty() ? self.core$().ownerAs(Namespace.class).qn() + "." : "";
		return prefix + self.name$();
	}

	public static String qn(Datalake.Tank self) {
		String namespace = namespace(self);
		String eventName = self.isMeasurement() ? self.asMeasurement().measurement.name$() : self.asMessage().message.name$();
		return qn(eventName, namespace.isEmpty() ? "" : namespace + ".", self);
	}

	private static String namespace(Datalake.Tank self) {
		return self.isMeasurement() ? eventNamespace(self.asMeasurement().measurement) : eventNamespace(self.asMessage().message);
	}

	private static String qn(String eventName, String prefix, Datalake.Tank tank) {
		if (tank.isSplitted()) {
			String split = tank.asSplitted().split().qn();
			return (split.isEmpty() ? "" : split + ".") + prefix + eventName;
		} else return prefix + eventName;
	}


	private static String eventNamespace(Message message) {
		return message.core$().owner().is(Namespace.class) ? message.core$().ownerAs(Namespace.class).qn() : "";
	}

	private static String eventNamespace(Measurement measurement) {
		return measurement.core$().owner().is(Namespace.class) ? measurement.core$().ownerAs(Namespace.class).qn() : "";
	}

	public static boolean isRoot(Datalake.Split self) {
		return !self.core$().owner().is(Datalake.Split.class);
	}

	public static boolean isLeaf(Datalake.Split self) {
		return self.core$().componentList().isEmpty();
	}

	public static List<Datalake.Split> leafs(Datalake.Split context) {
		if (context.isLeaf()) return singletonList(context);
		List<Datalake.Split> contexts = new ArrayList<>();
		for (Datalake.Split sub : context.splitList()) contexts.addAll(leafs(sub));
		return contexts;
	}
}
