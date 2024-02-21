package io.intino.ness.datalakeinspector.box;

import io.intino.alexandria.datalake.file.FileDatalake;

public class DatalakeInspectorBox extends AbstractBox {
	private FileDatalake datalake;

	public DatalakeInspectorBox(String[] args) {
		this(new DatalakeInspectorConfiguration(args));
	}

	public DatalakeInspectorBox(DatalakeInspectorConfiguration configuration) {
		super(configuration);
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public void beforeStart() {
		datalake = new FileDatalake(configuration.datalakeDirectory());
	}


	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}

	public FileDatalake datalake() {
		return datalake;
	}


	protected io.intino.alexandria.ui.services.AuthService authService(java.net.URL authServiceUrl) {
		return null;
	}
}