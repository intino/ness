package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.NessGraph;
import io.intino.ness.datahubterminalplugin.Formatters;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class MasterPublisher {
	private final NessGraph model;
	private final List<Datalake.Tank.Entity> tanks;
	private final File root;
	private final Configuration conf;
	private final Map<String, String> versions;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PluginLauncher.Notifier notifier;
	private final PrintStream logger;

	public MasterPublisher(File root, NessGraph model, List<Datalake.Tank.Entity> tanks, Configuration configuration, Map<String, String> versions, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.root = root;
		this.model = model;
		this.tanks = tanks;
		this.conf = configuration;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		this.notifier = notifier;
	}



	public void publish(){
		MasterRenderer renderer = new MasterRenderer(root, model, conf, logger, notifier);
		renderer.render();

	}
}
