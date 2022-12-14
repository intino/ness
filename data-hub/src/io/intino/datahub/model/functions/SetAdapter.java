package io.intino.datahub.model.functions;

import io.intino.alexandria.zet.ZetStream;
import io.intino.datahub.datalake.adapter.Context;

@FunctionalInterface
public interface SetAdapter {
	void adapt(ZetStream stream, Context context);
}
