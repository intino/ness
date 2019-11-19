package io.intino.ness.datahubterminalplugin;

import java.util.List;

public class Manifest {
	public List<String> publish;
	public List<String> subscribe;

	public Manifest(List<String> publish, List<String> subscribe) {
		this.publish = publish;
		this.subscribe = subscribe;
	}
}
