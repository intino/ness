package io.intino.master.core;

import io.intino.master.data.DatalakeLoader;
import io.intino.master.data.DefaultDatalakeLoader;
import io.intino.master.data.RecordTransformer;
import io.intino.master.data.validation.ValidationLayers;
import io.intino.master.serialization.MasterSerializer;
import io.intino.master.serialization.MasterSerializers;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MasterConfig {

	private File dataDirectory;
	private File logDirectory;
	private String instanceName = "master";
	private int port = 5701;
	private String host = "localhost";
	private MasterSerializer serializer = MasterSerializers.getDefault();
	private RecordTransformer transformer = RecordTransformer.dummy();
	private DatalakeLoader datalakeLoader = DatalakeLoader.createDefault();

	public MasterConfig() {
	}

	public MasterConfig(Map<String, String> arguments) {
		this.dataDirectory = new File(arguments.get("data_directory"));
		this.logDirectory = new File(arguments.get("log_directory"));
		this.instanceName = arguments.getOrDefault("instance_name", instanceName);
		this.port = Integer.parseInt(arguments.getOrDefault("port", String.valueOf(port)));
		this.serializer = MasterSerializers.get(arguments.getOrDefault("serializer", MasterSerializers.Standard.getDefault()));
		this.host = arguments.getOrDefault("host", host);
	}

	public MasterConfig(String[] args) {
		this(toMap(args));
	}

	public File dataDirectory() {
		return dataDirectory;
	}

	public MasterConfig dataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
		return this;
	}

	public File logDirectory() {
		return logDirectory;
	}

	public MasterConfig logDirectory(File logDirectory) {
		this.logDirectory = logDirectory;
		return this;
	}

	public String instanceName() {
		return instanceName;
	}

	public MasterConfig instanceName(String instanceName) {
		this.instanceName = instanceName;
		return this;
	}

	public int port() {
		return port;
	}

	public MasterConfig port(int port) {
		this.port = port;
		return this;
	}

	public String host() {
		return host;
	}

	public MasterConfig host(String host) {
		this.host = host;
		return this;
	}

	public MasterSerializer serializer() {
		return serializer;
	}

	public MasterConfig serializer(MasterSerializer serializer) {
		this.serializer = serializer;
		return this;
	}

	public RecordTransformer transformer() {
		return transformer;
	}

	public MasterConfig transformer(RecordTransformer transformer) {
		this.transformer = transformer;
		return this;
	}

	private static Map<String, String> toMap(String[] args) {
		return Arrays.stream(args).map(s -> s.split("=")).collect(Collectors.toMap(
				s -> s[0].trim(),
				s -> s[1].trim()
		));
	}

	public DatalakeLoader datalakeLoader() {
		return datalakeLoader;
	}

	public MasterConfig datalakeLoader(DatalakeLoader datalakeLoader) {
		this.datalakeLoader = datalakeLoader;
		return this;
	}
}
