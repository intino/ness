package io.intino.ness.graph.natives.tank;



/**#/Users/jevora/Documents/repositories/ness/application/src/io/intino/ness/Model.tara#12#1**/
public class Version_0 implements io.intino.tara.magritte.Expression<Integer> {
	private io.intino.ness.graph.Tank self;

	@Override
	public Integer value() {
		return io.intino.ness.Model.version(self);
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