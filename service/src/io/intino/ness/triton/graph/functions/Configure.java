package io.intino.ness.triton.graph.functions;

import io.intino.ness.triton.datalake.adapter.Context;

import java.io.InputStream;

@FunctionalInterface
public interface Configure {
	void configure(Context context, String configuration, InputStream attachment);
}
