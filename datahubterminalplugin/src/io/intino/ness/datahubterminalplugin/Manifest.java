package io.intino.ness.datahubterminalplugin;

import java.util.List;
import java.util.Map;

public class Manifest {
	public List<String> publish;
	public List<String> subscribe;
	public Map<String, String> tankClasses;

	public Manifest(List<String> publish, List<String> subscribe, Map<String, String> tankClasses) {
		this.publish = publish;
		this.subscribe = subscribe;
		this.tankClasses = tankClasses;
	}
}
