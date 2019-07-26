package io.intino.ness.datahub.graph.functions;

import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface SetAdapter {
	void adapt(ZetStream stream, Context context);
}
