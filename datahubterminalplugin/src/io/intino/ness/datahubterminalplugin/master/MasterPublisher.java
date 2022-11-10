package io.intino.ness.datahubterminalplugin.master;

import io.intino.Configuration;
import io.intino.datahub.model.NessGraph;
import io.intino.ness.datahubterminalplugin.Formatters;
import io.intino.ness.datahubterminalplugin.IntinoException;
import io.intino.ness.datahubterminalplugin.MavenTerminalExecutor;
import io.intino.ness.datahubterminalplugin.Version;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import static io.intino.ness.datahubterminalplugin.MavenTerminalExecutor.Target.Master;
import static io.intino.plugin.PluginLauncher.Phase.*;

public class MasterPublisher {
	private final NessGraph model;
	private final File root;
	private final Configuration conf;
	private final Map<String, String> versions;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PluginLauncher.Notifier notifier;
	private final PrintStream logger;

	public MasterPublisher(File root, NessGraph model, Configuration configuration, Map<String, String> versions, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.root = root;
		this.model = model;
		this.conf = configuration;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		this.notifier = notifier;
	}

	public boolean publish() {
		try {
			if (!checkPublish() || !createSources()) return false;
			logger.println("Publishing master...");
			new MavenTerminalExecutor(root, basePackage, Master, "master-terminal", versions, conf, systemProperties, logger)
					.mvn(invokedPhase == INSTALL ? "install" : "deploy");
			logger.println("Terminal master published!");
			return true;
		} catch (Throwable e) {
			logger.println(e.getMessage() == null ? e.toString() : e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private boolean createSources() {
		return new MasterRenderer(root, model, conf, logger, notifier).render();
	}

	private boolean checkPublish() {
		try {
			Version version = new Version(conf.artifact().version());
			if (!version.isSnapshot() && (invokedPhase == DISTRIBUTE || invokedPhase == DEPLOY))
				return false;
		} catch (IntinoException e) {
			return false;
		}
		return true;
	}

}
