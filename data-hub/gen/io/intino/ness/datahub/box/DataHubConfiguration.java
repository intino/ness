package io.intino.ness.datahub.box;

import java.util.Map;
import java.util.HashMap;

public class DataHubConfiguration extends io.intino.alexandria.core.BoxConfiguration {

	public DataHubConfiguration(String[] args) {
		super(args);
	}

	public String get(String key) {
		return args.get(key);
	}

	public String datalakePath() {
		return get("datalake_path");
	}

	public String brokerPath() {
		return get("broker_path");
	}

	public String apiPort() {
		return get("api_port");
	}

	public static java.net.URL url(String url) {
		try {
		return new java.net.URL(url);
		} catch (java.net.MalformedURLException e) {
			return null;
		}
	}
}