package io.intino.ness.natives.channel;



/**#/Users/oroncal/workspace/ness/application/src/Model.tara#11#1**/
public class Version_0 implements io.intino.tara.magritte.Expression<Integer> {
	private io.intino.ness.Channel self;

	@Override
	public Integer value() {
		return Model.version(self);
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