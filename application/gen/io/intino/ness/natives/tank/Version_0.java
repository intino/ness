package io.intino.ness.natives.tank;



/**#/Users/oroncal/workspace/ness/application/src/io/intino/ness/Model.tara#11#1**/
public class Version_0 implements io.intino.tara.magritte.Expression<Integer> {
	private io.intino.ness.Tank self;

	@Override
	public Integer value() {
		return io.intino.ness.Model.version(self);
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