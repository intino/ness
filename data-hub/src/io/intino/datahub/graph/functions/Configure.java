package io.intino.datahub.graph.functions;

import io.intino.datahub.datalake.adapter.Context;

import java.io.InputStream;

@FunctionalInterface
public interface Configure {
	void configure(Context context, String configuration, InputStream attachment);
}
