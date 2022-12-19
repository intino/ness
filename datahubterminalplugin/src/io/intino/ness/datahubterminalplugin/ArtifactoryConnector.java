package io.intino.ness.datahubterminalplugin;

import io.intino.Configuration;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
			return null;
		}
	}

	public static List<String> terminalVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/terminal-jms/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> ingestionVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/ingestion/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> masterVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/ness/master/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> eventVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/event/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> datalakeVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/datalake/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> bpmVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/bpm-framework/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}


	public static List<String> ledVersions() {
		try {
			URL url = new URL(INTINO_RELEASES + "/io/intino/alexandria/led/maven-metadata.xml");
			return extractVersions(read(connect(url)).toString());
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public static List<String> versions(Configuration.Repository repo, String artifact) {
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

	private static List<String> extractVersions(String metadata) {
		if (!metadata.contains("<versions>")) return Collections.emptyList();
		metadata = metadata.substring(metadata.indexOf("<versions>")).substring("<versions>".length() + 1);
		metadata = metadata.substring(0, metadata.indexOf("</versions>"));
		metadata = metadata.replace("<version>", "").replace("</version>", "");
		return Arrays.stream(metadata.trim().split("\n")).map(String::trim).collect(Collectors.toList());
	}

	private static ByteArrayOutputStream read(InputStream stream) throws Throwable {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (stream == null) return baos;
		try (stream) {
			byte[] byteChunk = new byte[4096];
			int n;
			while ((n = stream.read(byteChunk)) > 0)
				baos.write(byteChunk, 0, n);
		}
		return baos;
	}
}
