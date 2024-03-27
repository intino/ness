package io.intino.ness.terminal.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Manifest {
	private final String terminal;
	private final String qn;
	public List<String> publish;
	public List<String> subscribe;
	public Map<String, String> tankClasses;
	public Map<String, Set<String>> messageContexts;
	public List<String> parameters = Arrays.asList("terminal_url", "terminal_user", "terminal_password", "terminal_clientId", "terminal_working_directory");

	public Manifest(String name, String qn, List<String> publish, List<String> subscribe, Map<String, String> tankClasses, Map<String, Set<String>> messageContexts) {
		this.terminal = name;
		this.qn = qn;
		this.publish = publish;
		this.subscribe = subscribe;
		this.tankClasses = tankClasses;
		this.messageContexts = messageContexts;
	}
}
