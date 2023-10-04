package io.intino.ness.datahubterminalplugin;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ArtifactoryConnector {
	public static final String MAVEN_URL = "https://repo1.maven.org/maven2/";
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";

	private static InputStream connect(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			return connection.getInputStream();
		} catch (Throwable e) {
			return InputStream.nullInputStream();
		}
	}

	public static List<Version> chronosVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/sumus/chronos/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> terminalVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/terminal-jms/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> ingestionVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/ingestion/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> masterVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/ness/master/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> eventVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/event/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> datalakeVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/datalake/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<Version> bpmVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/bpm-framework/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}


	public static List<Version> versions(Configuration.Repository repo, String artifact) {
		try {
			String spec = repo.url() + (repo.url().endsWith("/") ? "" : "/") + artifact.replace(":", "/").replace(".", "/") + "/maven-metadata.xml";
			URL url = new URL(spec);
			final String mavenMetadata = read(connect(repo, url)).toString();
			if (!mavenMetadata.isEmpty()) return extractVersions(mavenMetadata);
		} catch (Throwable ignored) {
		}
		return Collections.emptyList();
	}


	private static InputStream connect(Configuration.Repository repository, URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			if (repository.user() != null) {
				String auth = repository.user() + ":" + repository.password();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				connection.setRequestProperty("Authorization", authHeaderValue);
				return connection.getInputStream();
			}
			return connect(url);
		} catch (Throwable e) {
			return null;
		}
	}

	private static List<Version> extractVersions(String metadata) {
		if (!metadata.contains("<versions>")) return Collections.emptyList();
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(version()).filter(Objects::nonNull).toList();
	}

	private static Function<String, Version> version() {
		return s -> {
			try {
				return new Version(s.trim());
			} catch (IntinoException e) {
				Logger.error(e);
				return null;
			}
		};
	}

	private static ByteArrayOutputStream read(InputStream stream) throws Throwable {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
		try (stream) {stream.transferTo(baos);}
		return baos;
	}
}
