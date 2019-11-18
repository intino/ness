package io.intino.datahub.graph;

import io.intino.datahub.graph.Datalake.Context;
import io.intino.tara.magritte.Node;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class Model {

	public static String schemaName(Data.Object self) {
		final io.intino.datahub.graph.Message schema = self.message();
		StringBuilder fullName = new StringBuilder();
		Node node = schema.core$();
		while (node.is(Message.class)) {
			fullName.insert(0, firstUpperCase(node.name()) + ".");
			node = node.owner();
		}
		return fullName.substring(0, fullName.length() - 1);
	}

	private static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	public static String qn(Context self) {
		String prefix = self.core$().owner().is(Context.class) && !self.core$().ownerAs(Context.class).qn().isEmpty() ? self.core$().ownerAs(Context.class).qn() + "." : "";
		return prefix + self.label();
	}

	public static String qn(Datalake.Tank self) {
		if (self.isContextual()) {
			String contextQn = self.asContextual().context().qn();
			return (contextQn.isEmpty() ? "" : contextQn + ".") + self.message().name$();
		} else return self.message().name$();
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
