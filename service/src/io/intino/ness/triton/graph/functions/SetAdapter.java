package io.intino.ness.triton.graph.functions;

import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.triton.datalake.adapter.Context;

@FunctionalInterface
public interface SetAdapter {
	void adapt(ZetStream stream, Context context);
}
