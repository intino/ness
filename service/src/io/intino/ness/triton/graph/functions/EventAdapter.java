package io.intino.ness.triton.graph.functions;

import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.triton.datalake.adapter.Context;

@FunctionalInterface
public interface EventAdapter {
	void adapt(ZimStream stream, Context context);
}
