import io.intino.Configuration;
import io.intino.ness.datahubterminalplugin.DataHubTerminalsPluginLauncher;
import io.intino.plugin.PluginLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;

public class PluginTest {
	@Test
	public void should_build_accessors() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.
				moduleStructure(new PluginLauncher.ModuleStructure(singletonList(new File(System.getProperty("user.home") + "/workspace/gestioncomercial/data-hub/src")), singletonList(new File(System.getProperty("user.home") + "/workspace/ness/datahubterminalplugin/test-res")), new File(System.getProperty("user.home") + "/workspace/ness/out/data-hub-test/")))
				.systemProperties(new PluginLauncher.SystemProperties(new File("/Applications/IntelliJ IDEA - 2019.2.app/Contents/plugins/maven/lib/maven3/"), new File("/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home")))
				.logger(System.out)
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(configuration());
		File temp = new File("/Users/oroncal/workspace/ness/datahubterminalplugin/temp");
		FileUtils.deleteDirectory(temp);
		temp.mkdirs();
		launcher.run(temp);
	}

	@Test
	public void should_build_cesar_terminal() throws IOException {
		DataHubTerminalsPluginLauncher launcher = new DataHubTerminalsPluginLauncher();
		launcher.
				moduleStructure(new PluginLauncher.ModuleStructure(singletonList(new File(System.getProperty("user.home") + "/workspace/cesar/data-hub/src")), singletonList(new File(System.getProperty("user.home") + "/workspace/cesar/data-hub/res")), new File(System.getProperty("user.home") + "/workspace/ness/out/data-hub-test/")))
				.systemProperties(new PluginLauncher.SystemProperties(new File("/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/"), new File("/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home")))
				.logger(System.out)
				.notifier(notifier())
				.invokedPhase(PluginLauncher.Phase.INSTALL)
				.moduleConfiguration(configuration());
		File temp = new File("/Users/oroncal/workspace/ness/datahubterminalplugin/temp");
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
				System.err.println(s);;
			}
		};
	}

	private Configuration configuration() {
		return new Configuration() {
			@Override
			public Artifact artifact() {
				return new Artifact() {
					@Override
					public String groupId() {
						return "io.intino.cesar";
					}

					@Override
					public String name() {
						return "datahub";
					}

					@Override
					public String version() {
						return "1.0.0";
					}

					@Override
					public void version(String s) {

					}

					@Override
					public Code code() {
						return null;
					}

					@Override
					public Model model() {
						return null;
					}

					@Override
					public Box box() {
						return null;
					}

					@Override
					public Dependency.DataHub datahub() {
						return null;
					}

					@Override
					public List<Dependency> dependencies() {
						return null;
					}

					@Override
					public List<WebComponent> webComponents() {
						return null;
					}

					@Override
					public List<WebResolution> webResolutions() {
						return null;
					}

					@Override
					public List<WebArtifact> webArtifacts() {
						return null;
					}

					@Override
					public List<Plugin> plugins() {
						return null;
					}

					@Override
					public Licence licence() {
						return null;
					}

					@Override
					public QualityAnalytics qualityAnalytics() {
						return null;
					}

					@Override
					public List<Parameter> parameters() {
						return null;
					}

					@Override
					public Package packageConfiguration() {
						return null;
					}

					@Override
					public Distribution distribution() {
						return null;
					}

					@Override
					public Configuration root() {
						return null;
					}

					@Override
					public ConfigurationNode owner() {
						return null;
					}
				};
			}

			@Override
			public List<Server> servers() {
				return null;
			}

			@Override
			public List<RunConfiguration> runConfigurations() {
				return null;
			}

			@Override
			public List<Repository> repositories() {
				return List.of(new Repository() {
					@Override
					public String identifier() {
						return "intino-maven";
					}

					@Override
					public String url() {
						return "https://artifactory.intino.io/artifactory/release-libraries";
					}

					@Override
					public Configuration root() {
						return null;
					}

					@Override
					public ConfigurationNode owner() {
						return null;
					}
				}, new Repository() {
					@Override
					public String identifier() {
						return "intino-maven";
					}

					@Override
					public String url() {
						return "https://artifactory.intino.io/artifactory/release-libraries";
					}

					@Override
					public Configuration root() {
						return null;
					}

					@Override
					public ConfigurationNode owner() {
						return null;
					}
				});
			}
		};
	}
}