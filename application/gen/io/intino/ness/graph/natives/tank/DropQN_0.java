package io.intino.ness.graph.natives.tank;



/**#/Users/oroncal/workspace/ness/application/src/io/intino/ness/Model.tara#12#1**/
public class DropQN_0 implements io.intino.tara.magritte.Expression<String> {
	private io.intino.ness.graph.Tank self;

	@Override
	public String value() {
		return "drop." + self.qualifiedName();
	}

	@Override
	public void self(io.intino.tara.magritte.Layer context) {
		self = (io.intino.ness.graph.Tank) context;
	}

	@Override
	public Class<? extends io.intino.tara.magritte.Layer> selfClass() {
		return io.intino.ness.graph.Tank.class;
	}
}