package io.intino.ness.box;

public class NessServiceBox extends AbstractBox {

	public NessServiceBox(String[] args) {
		super(args);
	}

	public NessServiceBox(NessServiceConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public io.intino.alexandria.core.Box open() {
		return super.open();
	}

	public void close() {
		super.close();
	}
}