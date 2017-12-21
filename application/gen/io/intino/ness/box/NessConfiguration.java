package io.intino.ness.box;

public class NessConfiguration extends io.intino.konos.alexandria.BoxConfiguration {

	NessieConfiguration nessieConfiguration;

	public NessConfiguration(String[] args) {
		super(args);
		fillWithArgs();
	}

	private void fillWithArgs() {
		if (this.store == null && args.get("graph_store") != null)
			store = new java.io.File(args.remove("graph_store"));
		if (args.containsKey("nessie_token"))
			nessieConfiguration(args.remove("nessie_token"));
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