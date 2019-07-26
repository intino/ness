package io.intino.ness.datahub.graph.natives.broker;

import io.intino.ness.datahub.broker.BrokerService;

/**Broker#/Users/oroncal/workspace/ness/data-hub/src/io/intino/ness/datahub/graph/Model.tara#6#1**/
public class Implementation_0 implements io.intino.ness.datahub.graph.functions.BrokerImplementation, io.intino.tara.magritte.Function {
	private io.intino.ness.datahub.graph.Broker self;

	@Override
	public BrokerService get() {
		return null;
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.ness.datahub.graph.Broker) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.ness.datahub.graph.Broker.class;
	}
}