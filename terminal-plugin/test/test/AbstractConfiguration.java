package test;

import io.intino.Configuration;

import java.util.List;

public class AbstractConfiguration implements Configuration {

	@Override
	public Artifact artifact() {
		return null;
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
		return null;
	}

	public static class AbstractArtifact implements Artifact {

		@Override
		public String groupId() {
			return null;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public void name(String s) {

		}

		@Override
		public String version() {
			return null;
		}

		@Override
		public String description() {
			return null;
		}

		@Override
		public String url() {
			return null;
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
		public Dependency.Archetype archetype() {
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
		public License license() {
			return null;
		}

		@Override
		public Scm scm() {
			return null;
		}

		@Override
		public List<Developer> developers() {
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

		public static class AbstractCode implements Code {

			@Override
			public String generationPackage() {
				return null;
			}

			@Override
			public String nativeLanguage() {
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
		}
	}
}
