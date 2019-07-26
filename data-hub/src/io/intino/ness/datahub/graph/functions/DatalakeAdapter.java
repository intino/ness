package io.intino.ness.datahub.graph.functions;


import io.intino.alexandria.datalake.Datalake;
import io.intino.ness.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface DatalakeAdapter {


	void adapt(Datalake datalake, Context context);

}
