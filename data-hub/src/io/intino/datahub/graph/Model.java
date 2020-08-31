package io.intino.datahub.graph;

import io.intino.datahub.graph.Datalake.Split;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class Model {

	public static String qn(Split self) {
		String prefix = self.core$().owner().is(Split.class) && !self.core$().ownerAs(Split.class).qn().isEmpty() ? self.core$().ownerAs(Split.class).qn() + "." : "";
		return prefix + self.label();
	}

	public static String qn(Namespace self) {
		String prefix = self.core$().owner().is(Namespace.class) && !self.core$().ownerAs(Namespace.class).qn().isEmpty() ? self.core$().ownerAs(Namespace.class).qn() + "." : "";
		return prefix + self.name$();
	}

	public static String qn(Datalake.Tank.Event self) {
		String namespace = eventNamespace(self.event);
		String prefix = namespace.isEmpty() ? "" : namespace + ".";
		if (self.asTank().isSplitted()) {
			String contextQn = self.asTank().asSplitted().split().qn();
			return (contextQn.isEmpty() ? "" : contextQn + ".") + prefix + self.event().name$();
		} else return prefix + self.event().name$();
	}

	private static String eventNamespace(Event event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn() : "";
	}

	public static boolean isRoot(Split self) {
		return !self.core$().owner().is(Split.class);
	}

	public static boolean isLeaf(Split self) {
		return self.core$().componentList().isEmpty();
	}

	public static List<Split> leafs(Split context) {
		if (context.isLeaf()) return singletonList(context);
		List<Datalake.Split> contexts = new ArrayList<>();
		for (Datalake.Split sub : context.splitList()) contexts.addAll(leafs(sub));
		return contexts;
	}
}
