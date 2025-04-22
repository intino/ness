package systems.intino.eventsourcing.datahubbuilder;

import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datahubbuilder.util.Version;

import java.util.Comparator;
import java.util.List;

public class VersionBounds {
	static final String MINIMUM_SUBJECT_STORE_VERSION = "2.0.2";
	static final String MAX_SUBJECT_STORE_VERSION = "3.0.0";
	static final String MINIMUM_BPM_VERSION = "3.1.1";
	static final String MAX_BPM_VERSION = "4.0.0";
	static final String MINIMUM_TERMINAL_VERSION = "6.2.0";
	static final String MAX_TERMINAL_VERSION = "7.0.0";
	static final String MINIMUM_INGESTION_VERSION = "5.1.0";
	static final String MAX_INGESTION_VERSION = "6.0.0";
	static final String MINIMUM_EVENT_VERSION = "5.1.0";
	static final String MAX_EVENT_VERSION = "6.0.0";


	static String terminalVersion() {
		return suitableVersion(ArtifactoryConnector.terminalVersions(), MINIMUM_TERMINAL_VERSION, MAX_TERMINAL_VERSION);
	}

	static String ingestionVersion() {
		return suitableVersion(ArtifactoryConnector.ingestionVersions(), MINIMUM_INGESTION_VERSION, MAX_INGESTION_VERSION);
	}

	static String eventVersion() {
		return suitableVersion(ArtifactoryConnector.eventVersions(), MINIMUM_EVENT_VERSION, MAX_EVENT_VERSION);
	}

	static String subjectStoreVersion() {
		return suitableVersion(ArtifactoryConnector.subjectStoreVersions(), MINIMUM_SUBJECT_STORE_VERSION, MAX_SUBJECT_STORE_VERSION);
	}

	static String bpmVersion() {
		return suitableVersion(ArtifactoryConnector.bpmVersions(), MINIMUM_BPM_VERSION, MAX_BPM_VERSION);
	}

	private static String suitableVersion(List<Version> versions, String min, String max) {
		try {
			Version maxVersion = new Version(max);
			Version minVersion = new Version(min);
			return versions.stream().sorted(Comparator.reverseOrder()).filter(v -> v.compareTo(maxVersion) < 0).findFirst().orElse(minVersion).get();
		} catch (IntinoException e) {
			Logger.error(e);
			return min;
		}
	}
}
