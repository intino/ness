package io.intino.ness.konos;

public class NessConfiguration extends io.intino.konos.BoxConfiguration {

	NessieConfiguration nessieConfiguration;
	TopicsConfiguration topicsConfiguration;

	public NessConfiguration(String[] args) {
		super(args);
		fillWithArgs();
	}

	private void fillWithArgs() {
		if (this.store == null && args.get("graph.store") != null)
			store = new java.io.File(args.remove("graph.store"));
		if (args.containsKey("nessie.token"))
			nessieConfiguration(args.remove("nessie.token"));
		if (args.containsKey("topics.url"))
			topicsConfiguration(args.remove("topics.url"), args.remove("topics.user"), args.remove("topics.password"), args.remove("topics.clientID"), args.remove("topics.productionPaths"));


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

	public NessConfiguration topicsConfiguration(String url, String user, String password, String clientID, String... productionPaths) {
		this.topicsConfiguration = new TopicsConfiguration();
		this.topicsConfiguration.url = url;
		this.topicsConfiguration.user = user;
		this.topicsConfiguration.password = password;
		this.topicsConfiguration.clientID = clientID;
		this.topicsConfiguration.productionPaths = java.util.Arrays.asList(productionPaths);
		return this;
	}

	public TopicsConfiguration topicsConfiguration() {
		return this.topicsConfiguration;
	}

	public static class NessieConfiguration {
		public String token;
	}

	public static class TopicsConfiguration {
		public String url;
		public String user;
		public String password;
		public String clientID;
		public java.util.List<String> productionPaths;
	}
}