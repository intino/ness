package io.intino.ness.builder.test;

import io.intino.Configuration;

import java.util.List;

public class ConfigurationBuilder {

	private Configuration.Artifact artifact;
	private List<Configuration.Repository> repositories = getDefaultRepositories();

	public Configuration build() {
		return new AbstractConfiguration() {
			@Override
			public Artifact artifact() {
				return artifact;
			}

			@Override
			public List<Repository> repositories() {
				return repositories;
			}
		};
	}

	public ArtifactBuilder artifactBegin() {
		return new ArtifactBuilder();
	}

	public ConfigurationBuilder repositories(List<Configuration.Repository> repositories) {
		this.repositories = repositories;
		return this;
	}

	public class ArtifactBuilder {
		private String groupId;
		private String name;
		private String version;
		private String codeGenerationPackage;

		public ConfigurationBuilder artifactEnd() {
			artifact = new AbstractConfiguration.AbstractArtifact() {
				@Override
				public String groupId() {
					return groupId;
				}

				@Override
				public String name() {
					return name;
				}

				@Override
				public String version() {
					return version;
				}

				@Override
				public Code code() {
					return codeGenerationPackage == null ? null : new AbstractCode() {
						@Override
						public String generationPackage() {
							return codeGenerationPackage;
						}
					};
				}
			};

			return ConfigurationBuilder.this;
		}

		public ArtifactBuilder groupId(String groupId) {
			this.groupId = groupId;
			return this;
		}

		public ArtifactBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ArtifactBuilder version(String version) {
			this.version = version;
			return this;
		}

		public ArtifactBuilder codeGenerationPackage(String codeGenerationPackage) {
			this.codeGenerationPackage = codeGenerationPackage;
			return this;
		}
	}

	private static List<Configuration.Repository> getDefaultRepositories() {
		return List.of(new Configuration.Repository() {
			@Override
			public String identifier() {
				return "intino-maven";
			}

			@Override
			public String url() {
				return "https://artifactory.intino.io/artifactory/release-libraries";
			}

			@Override
			public String user() {
				return null;
			}

			@Override
			public String password() {
				return null;
			}

			@Override
			public UpdatePolicy updatePolicy() {
				return UpdatePolicy.Daily;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		}, new Configuration.Repository() {
			@Override
			public String identifier() {
				return "intino-maven";
			}

			@Override
			public String url() {
				return "https://artifactory.intino.io/artifactory/release-libraries";
			}

			@Override
			public String user() {
				return null;
			}

			@Override
			public String password() {
				return null;
			}

			@Override
			public UpdatePolicy updatePolicy() {
				return UpdatePolicy.Daily;
			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		});
	}

}
