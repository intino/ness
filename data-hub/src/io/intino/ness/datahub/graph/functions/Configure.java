package io.intino.ness.datahub.graph.functions;

import io.intino.ness.datahub.datalake.adapter.Context;

import java.io.InputStream;

@FunctionalInterface
public interface Configure {
	void configure(Context context, String configuration, InputStream attachment);
}
