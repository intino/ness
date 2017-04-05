package io.intino.ness.konos;

public class NessConfiguration extends io.intino.konos.BoxConfiguration {

	NessieConfiguration nessieConfiguration;

	public NessConfiguration(String[] args) {
		super(args);
		fillWithArgs();
	}

	private void fillWithArgs() {
		if (this.store == null && args.get("graph.store") != null)
			store = new java.io.File(args.remove("graph.store"));
		if (args.containsKey("nessie.token"))
			nessieConfiguration(args.remove("nessie.token"));

	}

	public java.io.File store() {
		return this.store;
	}

	public NessConfiguration nessieConfiguration(String token) {
		this.nessieConfiguration = new NessieConfiguration();
		this.nessieConfiguration.token = token;
		return this;
	}

	public NessieConfiguration nessieConfiguration() {
		return this.nessieConfiguration;
	}

	public static class NessieConfiguration {
		public String token;
	}
}