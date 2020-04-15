package io.intino.datahub.graph;

import io.intino.datahub.graph.Datalake.Context;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class Model {

	public static String qn(Context self) {
		String prefix = self.core$().owner().is(Context.class) && !self.core$().ownerAs(Context.class).qn().isEmpty() ? self.core$().ownerAs(Context.class).qn() + "." : "";
		return prefix + self.label();
	}

	public static String qn(Datalake.Tank.EventTankType self) {
		if (self.asTank().isContextual()) {
			String contextQn = self.asTank().asContextual().context().qn();
			return (contextQn.isEmpty() ? "" : contextQn + ".") + self.event().name$();
		} else return self.event().name$();
	}

	public static String qn(Datalake.Tank.SessionEventTank self) {
		if (self.asTank().isContextual()) {
			String contextQn = self.asTank().asContextual().context().qn();
			return (contextQn.isEmpty() ? "" : contextQn + ".") + self.sessionEventClass();
		} else return self.sessionEventClass();
	}

	public static boolean isRoot(Context self) {
		return !self.core$().owner().is(Context.class);
	}

	public static boolean isLeaf(Context self) {
		return self.core$().componentList().isEmpty();
	}

	public static List<Context> leafs(Context context) {
		if (context.isLeaf()) return singletonList(context);
		List<Datalake.Context> contexts = new ArrayList<>();
		for (Datalake.Context sub : context.contextList()) contexts.addAll(leafs(sub));
		return contexts;
	}
}
