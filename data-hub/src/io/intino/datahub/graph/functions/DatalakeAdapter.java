package io.intino.datahub.graph.functions;


import io.intino.alexandria.datalake.Datalake;
import io.intino.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface DatalakeAdapter {


	void adapt(Datalake datalake, Context context);

}
