package io.intino.ness.datahub.graph.functions;

import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface EventAdapter {
	void adapt(ZimStream stream, Context context);
}
