package io.intino.ness.datahubterminalplugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Manifest {
	private final String terminal;
	private final String qn;
	public List<String> publish;
	public List<String> subscribe;
	public Map<String, String> tankClasses;
	public final List<String> parameters = Arrays.asList("terminal_url", "terminal_user", "terminal_password", "terminal_clientId", "terminal_working_directory");

	public Manifest(String name, String qn, List<String> publish, List<String> subscribe, Map<String, String> tankClasses) {
		this.terminal = name;
		this.qn = qn;
		this.publish = publish;
		this.subscribe = subscribe;
		this.tankClasses = tankClasses;
	}
}
