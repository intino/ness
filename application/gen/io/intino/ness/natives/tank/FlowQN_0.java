package io.intino.ness.natives.tank;



/**#/Users/oroncal/workspace/ness/application/src/io/intino/ness/Model.tara#9#1**/
public class FlowQN_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.ness.Tank self;

	@Override
	public String value() {
		return "flow." + self.qualifiedName();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.ness.Tank) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.ness.Tank.class;
	}
}