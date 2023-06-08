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
	public boolean datamartsAutoLoad;
	public final List<String> connectionParameters = Arrays.asList("datahub_url", "datahub_user", "datahub_password", "datahub_clientId", "keystore_file",
			"truststore_file", "keystore_password", "truststore_password");
	public final List<String> additionalParameters = List.of("datahub_outbox_directory");

	public Manifest(String name, String qn, List<String> publish, List<String> subscribe, Map<String, String> tankClasses, boolean datamartsAutoLoad) {
		this.terminal = name;
		this.qn = qn;
		this.publish = publish;
		this.subscribe = subscribe;
		this.tankClasses = tankClasses;
		this.datamartsAutoLoad = datamartsAutoLoad;
	}
}
