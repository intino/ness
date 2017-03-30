package io.intino.ness.datalake.konos;

public class DatalakeConfiguration extends io.intino.konos.BoxConfiguration {

	NessConfiguration nessConfiguration;

	public DatalakeConfiguration(String[] args) {
		super(args);
		fillWithArgs();
	}

	private void fillWithArgs() {

		if (args.containsKey("ness.url"))
			nessConfiguration(args.remove("ness.url"), args.remove("ness.user"), args.remove("ness.password"), args.remove("ness.clientID"), args.remove("ness.productionPaths"));

	}



	public DatalakeConfiguration nessConfiguration(String url, String user, String password, String clientID, String... productionPaths) {
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

	public static class NessConfiguration {
		public String url;
		public String user;
		public String password;
		public String clientID;
		public java.util.List<String> productionPaths;
	}
}