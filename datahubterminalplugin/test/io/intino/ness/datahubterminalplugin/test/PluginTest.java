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

import static io.intino.ness.datahubterminalplugin.test.OSValidator.isWindows;
import static java.util.Collections.singletonList;

@Ignore
public class PluginTest {
	private static final String USER_HOME = System.getProperty("user.home");
	private static final String NESS_DIR = isWindows() ? "C:\\Users\\naits\\Desktop\\IntinoDev\\ness\\" : "/Users/oroncal/workspace/infrastructure/ness/";
	private static final String WORKSPACE_ROOT = isWindows() ? "C:/Users/naits/Desktop/" : USER_HOME + "/workspace";
	private static final String TEST_MODULE_PATH = isWindows() ? "C:/Users/naits/Desktop/IntinoDev/ness/test" : USER_HOME + "/workspace/infrastructure/ness/test";
	private static final String INTELLIJ_MAVEN_PLUGIN = isWindows() ? "C:\\Users\\naits\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-C\\ch-0\\223.8617.56\\plugins\\maven\\lib\\maven3" : "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/";
	private static final String JAVA_HOME = isWindows() ? System.getenv("JAVA_HOME") : "/Users/oroncal/Library/Java/JavaVirtualMachines/openjdk-17.0.1/Contents/Home";
	private static final String COSMOS_PROJECT_PATH = isWindows() ? "C:/Users/naits/Desktop/IntinoDev/cesar/datahub/" : USER_HOME + "/workspace/infrastructure/cosmos/datahub/";

	@Test
	public void should_build_cosmos_terminal() {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.deleteTempDirOnPublish(false);
		launcher.
				moduleStructure(new ModuleStructure(singletonList(
						new File(COSMOS_PROJECT_PATH + "src")),
						singletonList(new File(COSMOS_PROJECT_PATH + "res")),
						new File(COSMOS_PROJECT_PATH + "out/test/")))
				.systemProperties(new SystemProperties(new File(INTELLIJ_MAVEN_PLUGIN),
						new File(JAVA_HOME)))
				.logger(System.out)
				.notifier(notifier())
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(cosmosConfiguration());
		File temp = new File(NESS_DIR + "/datahubterminalplugin/temp/cosmos");
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_test_terminals() {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.deleteTempDirOnPublish(false);
		launcher.publishTerminalsIfOntologyFails(true);
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
	public void should_build_gc_terminals() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.
				moduleStructure(new ModuleStructure(List.of(new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/src"), new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/shared")), singletonList(new File(USER_HOME + "/workspace/cfe/suministro/core/data-hub-ng/res")), new File(USER_HOME + "/workspace/infrastructure/ness/out/data-hub-ng-test/")))
				.systemProperties(new SystemProperties(new File("/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/"), new File("/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home")))
				.logger(System.out)
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(gcConfiguration());
		File temp = new File(USER_HOME + "/workspace/infrastructure/ness/datahubterminalplugin/temp/gc");
		FileUtils.deleteDirectory(temp);
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_cinepolis_terminals() throws IOException, InterruptedException {
//		Thread.sleep(20000);
		long start = System.currentTimeMillis();
		new File(WORKSPACE_ROOT, "IntinoDev/ness/out/datahub-cinepolis/").mkdirs();
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.deleteTempDirOnPublish(false);
		launcher.moduleStructure(new ModuleStructure(List.of(
						new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/src"),
						new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/shared")),
						singletonList(new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/datahub/res")),
						new File(WORKSPACE_ROOT, "MonentiaDev/cinepolis/out")))
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
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + (time / 1000.0f) + " seconds");
//		FileUtils.deleteDirectory(temp);
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


	private Configuration cosmosConfiguration() {
		return new ConfigurationBuilder()
				.artifactBegin()
				.groupId("io.intino.cosmos")
				.name("datahub")
				.version("1.0.0")
				.codeGenerationPackage("io.intino.cosmos.datahub")
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