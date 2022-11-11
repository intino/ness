package io.intino.ness.datahubterminalplugin.test;

import io.intino.Configuration;
import io.intino.ness.datahubterminalplugin.DataHubTerminalsPluginLauncher;
import io.intino.plugin.PluginLauncher;
import io.intino.plugin.PluginLauncher.ModuleStructure;
import io.intino.plugin.PluginLauncher.SystemProperties;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;

@Ignore
public class PluginTest {

	private static final String NESS_DIR = "C:\\Users\\naits\\Desktop\\IntinoDev\\ness\\";
	private static final String MODULE_DIR = "datahubterminalplugin";
	private static final File ModuleFile = new File(MODULE_DIR);
	private static final String USER_HOME = System.getProperty("user.home");
//	private static final String WORKSPACE_ROOT = "C:/Users/naits/Desktop/";
	//	private static final String TEST_MODULE_PATH = USER_HOME + "/workspace/ness/datahubterminalplugin/temp/test";
	private static final String TEST_MODULE_PATH = "C:/Users/naits/Desktop/IntinoDev/ness/test";
		private static final String WORKSPACE_ROOT = USER_HOME + "/workspace";
//	public static final String INTELLIJ_MAVEN_PLUGIN = "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/";
		private static final String INTELLIJ_MAVEN_PLUGIN = "C:/Users/naits/AppData/Local/JetBrains/Toolbox/apps/IDEA-C/ch-0/222.3739.54/plugins/maven/lib/maven3";
//	private static final String JAVA_HOME = "/Library/Java/JavaVirtualMachines/jdk-11.0.10.jdk/Contents/Home";
	private static final String JAVA_HOME = "C:/Program Files/Java/jdk-11.0.2";

	@Test
	public void should_build_test_terminals() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.moduleStructure(new ModuleStructure(List.of(
						new File(TEST_MODULE_PATH + "/src")),
						List.of(new File(TEST_MODULE_PATH + "/res")),
						new File(NESS_DIR + "/out")))
				.systemProperties(new SystemProperties(new File(INTELLIJ_MAVEN_PLUGIN),
						new File(JAVA_HOME)))
				.logger(System.out)
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.notifier(notifier())
				.moduleConfiguration(testConfiguration());
		File temp = new File(NESS_DIR + "/datahubterminalplugin/temp/test");
//		FileUtils.deleteDirectory(temp);
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_cesar_terminal() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.
				moduleStructure(new ModuleStructure(singletonList(new File(USER_HOME + "/workspace/cesar/datahub/src")), singletonList(new File(USER_HOME + "/workspace/cesar/datahub/res")), new File(USER_HOME + "/workspace/ness/out/test/")))
				.systemProperties(new SystemProperties(new File(INTELLIJ_MAVEN_PLUGIN),
						new File(JAVA_HOME)))
				.logger(System.out)
				.notifier(notifier())
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(cesarConfiguration());
		File temp = new File(USER_HOME + "/workspace/ness/datahubterminalplugin/temp/cesar");
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_gc_terminals() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.
				moduleStructure(new ModuleStructure(List.of(new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/src"), new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/shared")), singletonList(new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/res")), new File(USER_HOME + "/workspace/ness/out/data-hub-ng-test/")))
				.systemProperties(new SystemProperties(new File("/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/"), new File("/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home")))
				.logger(System.out)
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(gcConfiguration());
		File temp = new File(USER_HOME + "/workspace/ness/datahubterminalplugin/temp/gc");
		FileUtils.deleteDirectory(temp);
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_cinepolis_terminals() throws IOException {
		new File(WORKSPACE_ROOT, "IntinoDev/ness/out/datahub-cinepolis/").mkdirs();
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.moduleStructure(new ModuleStructure(List.of(
						new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/src"),
						new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/shared")),
						singletonList(new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/res")),
						new File(WORKSPACE_ROOT,"MonentiaDev/cinepolis/out")))
				.systemProperties(new SystemProperties(
						new File(INTELLIJ_MAVEN_PLUGIN),
						new File(JAVA_HOME)))
				.logger(System.out)
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(cinepolisConfiguration());
		File temp = new File(NESS_DIR + "/datahubterminalplugin/temp/cinepolis");
		FileUtils.deleteDirectory(temp);
		temp.mkdirs();
		launcher.run(temp);
	}

	private PluginLauncher.Notifier notifier() {
		return new PluginLauncher.Notifier() {
			@Override
			public void notify(String s) {
				System.out.println(s);
			}

			@Override
			public void notifyError(String s) {
				System.err.println(s);
			}
		};
	}


	private Configuration cesarConfiguration() {
		return new ConfigurationBuilder()
				.artifactBegin()
					.groupId("io.intino.cesar")
					.name("datahub")
					.version("1.0.0")
					.codeGenerationPackage("io.intino.cesar.datahub")
				.artifactEnd()
				.build();
	}

	private Configuration gcConfiguration() {
		return new ConfigurationBuilder()
				.artifactBegin()
					.groupId("io.provista")
					.name("datahub")
					.version("3.1.0")
				.artifactEnd()
				.build();
	}

	private Configuration cinepolisConfiguration() {
		return new ConfigurationBuilder()
				.artifactBegin()
					.groupId("com.cinepolis")
					.name("datahub")
					.version("2.0.0")
					.codeGenerationPackage("com.cinepolis.datahub")
				.artifactEnd()
				.build();
	}

	private Configuration testConfiguration() {
		return new ConfigurationBuilder()
				.artifactBegin()
					.groupId("io.intino.test")
					.name("datahub-test")
					.version("1.0.0-SNAPSHOT")
					.codeGenerationPackage("org.example.test.model")
				.artifactEnd()
				.build();
	}
}