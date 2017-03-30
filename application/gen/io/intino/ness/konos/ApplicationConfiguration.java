package io.intino.ness.konos;

public class ApplicationConfiguration extends io.intino.konos.BoxConfiguration {

	NessyConfiguration nessyConfiguration;
	NessConfiguration nessConfiguration;

	public ApplicationConfiguration(String[] args) {
		super(args);
		fillWithArgs();
	}

	private void fillWithArgs() {
		if (this.store == null && args.get("graph.store") != null)
			store = new java.io.File(args.remove("graph.store"));
		if (args.containsKey("nessy.token"))
			nessyConfiguration(args.remove("nessy.token"));
		if (args.containsKey("ness.url"))
			nessConfiguration(args.remove("ness.url"), args.remove("ness.user"), args.remove("ness.password"), args.remove("ness.clientID"), args.remove("ness.productionPaths"));


	}

	public java.io.File store() {
		return this.store;
	}

	public ApplicationConfiguration nessyConfiguration(String token) {
		this.nessyConfiguration = new NessyConfiguration();
		this.nessyConfiguration.token = token;
		return this;
	}

	public NessyConfiguration nessyConfiguration() {
		return this.nessyConfiguration;
	}

	public ApplicationConfiguration nessConfiguration(String url, String user, String password, String clientID, String... productionPaths) {
		this.nessConfiguration = new NessConfiguration();
		this.nessConfiguration.url = url;
		this.nessConfiguration.user = user;
		this.nessConfiguration.password = password;
		this.nessConfiguration.clientID = clientID;
		this.nessConfiguration.productionPaths = java.util.Arrays.asList(productionPaths);
		return this;
	}

	public NessConfiguration nessConfiguration() {
		return this.nessConfiguration;
	}

	public static class NessyConfiguration {
		public String token;
	}

	public static class NessConfiguration {
		public String url;
		public String user;
		public String password;
		public String clientID;
		public java.util.List<String> productionPaths;
	}
}