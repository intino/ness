package io.intino.ness.triton.graph.functions;

import io.intino.ness.datalake.Datalake;
import io.intino.ness.triton.datalake.adapter.Context;

@FunctionalInterface
public interface DatalakeAdapter {
	void adapt(Datalake datalake, Context context);
}
