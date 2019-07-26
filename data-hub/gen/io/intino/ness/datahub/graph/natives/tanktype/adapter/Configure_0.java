package io.intino.ness.datahub.graph.natives.tanktype.adapter;

import io.intino.ness.datahub.datalake.adapter.Context;
import java.io.InputStream;

/**TankType:Adapter#/Users/oroncal/workspace/ness/data-hub/src/io/intino/ness/datahub/graph/Model.tara#37#1**/
public class Configure_0 implements io.intino.ness.datahub.graph.functions.Configure, io.intino.tara.magritte.Function {
	private io.intino.ness.datahub.graph.tanktype.TankTypeAdapter self;

	@Override
	public void configure(Context context, String configuration, InputStream attachment) {
		;
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.ness.datahub.graph.tanktype.TankTypeAdapter) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.ness.datahub.graph.tanktype.TankTypeAdapter.class;
	}
}