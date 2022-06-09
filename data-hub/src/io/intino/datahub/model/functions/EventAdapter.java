package io.intino.datahub.model.functions;

import io.intino.alexandria.zim.ZimStream;
import io.intino.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface EventAdapter {
	void adapt(ZimStream stream, Context context);
}
