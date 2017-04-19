package io.intino.ness.natives.channel;



/**#/Users/oroncal/workspace/ness/application/src/Model.tara#9#1**/
public class FlowQN_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.ness.Channel self;

	@Override
	public String value() {
		return "flow." + self.qualifiedName();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.ness.Channel) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.ness.Channel.class;
	}
}