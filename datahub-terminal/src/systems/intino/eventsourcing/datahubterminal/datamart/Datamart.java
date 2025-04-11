package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datahubterminal.Connector;
import systems.intino.eventsourcing.datahubterminal.StubConnector;
import systems.intino.eventsourcing.event.Event;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Datamart {
	private final Connector connector;
	private final Map<String, Map<String, SubjectNode>> subjects = new HashMap<>();
	private final AtomicBoolean initializing = new AtomicBoolean(false);
	private final DatamartAccessor datamartAccessor;
	private Instant ts;
	protected boolean localAccess;
	protected final Map<String, List<SubjectListener<SubjectWrapper>>> subjectListeners = new HashMap<>();
	protected final Map<String, SubjectMounter> mounters = new HashMap<>();

	public Datamart(String name, Connector connector, ConnectionConfig connectionConfig) {
		this.connector = requireNonNull(connector);
		this.datamartAccessor = new DatamartAccessor(name, connector, connectionConfig);
	}

	public synchronized Datamart init(String datamartSourceSelector) {
		try {
			initializing.set(true);
			downloadDatamartFromDatahub(datamartSourceSelector);
			Logger.info("MasterDatamart (" + connector.clientId() + ") initialized successfully.");
		} catch (Exception e) {
			throw new ExceptionInInitializerError("MasterDatamart failed to start because a " + e.getClass().getName() + " occurred: " + e.getMessage());
		} finally {
			initializing.set(false);
		}
		return this;
	}

	public Instant ts() {
		return ts;
	}

	public int size() {
		return subjects.size();
	}

	public boolean localAccess() {
		return localAccess;
	}

	public SubjectNode createSubject(String type, String id) {
		//TODO
		return null;
	}

	public SubjectNode getSubject(String type, String id) {
		return subjects.get(type).get(id);
	}

	public SubjectNode getSubject(String[] possibleTypes, String id) {
		return Arrays.stream(possibleTypes).map(subjects::get).map(m -> m.get(id)).filter(Objects::nonNull).findFirst().orElse(null);
	}

	public Stream<SubjectNode> subjects() {
		return subjects.values().stream().flatMap(m -> m.values().stream());
	}

	public Stream<SubjectNode> subjects(String type) {
		return subjects.get(type).values().stream();
	}

	public Stream<SubjectNode> subjects(String... types) {
		return Arrays.stream(types)
				.map(subjects::get)
				.filter(Objects::nonNull)
				.flatMap(s -> s.values().stream());
	}

	public void register(String type, SubjectMounter mounter) {
		mounters.put(type, mounter);
	}

	public void addListener(String type, SubjectListener<SubjectWrapper> listener) {
		if (listener == null) throw new NullPointerException("SubjectListener cannot be null");
		subjectListeners.putIfAbsent(type, new ArrayList<>());
		subjectListeners.get(type).add(listener);
	}

	public List<SubjectListener<SubjectWrapper>> listenersOf(String type) {
		return subjectListeners.get(type);
	}

	public synchronized void handleDatahubNotification(String notification) {
		try {
			if (notification == null || notification.isBlank()) return;
			String[] typeAndSs = notification.split("\0");
			handleSubjectNotification(typeAndSs[0], typeAndSs[1]);
		} catch (Throwable e) {
			Logger.error("Error while processing datahub notification " + notification + ": " + e.getMessage(), e);
		}
	}

	private void handleSubjectNotification(String type, String id) {
		//TODO
	}

	private String normalizedId(String id, String type) {
		return id.replace(":", "-") + ":" + type;
	}

	public synchronized void mount(Event event) {
		try {
			if (event == null) return;
			this.ts = event.ts();
			mountSubjects(event);
		} catch (Throwable e) {
			Logger.error("Error while mounting event ss=" + event.ss() + ", ts=" + event.ts() + " -> " + e.getMessage(), e);
		}
	}

	private void mountSubjects(Event event) {
		try {
			var mounter = this.mounters.get(event.type());
			if (mounter != null) mounter.useListeners(!this.initializing.get()).mount(event);
		} catch (Exception e) {
			Logger.error("Failed to mount event of type " + event.type() + ": " + e.getMessage(), e);
		}
	}

	private void downloadDatamartFromDatahub(String selector) {
		if (connector instanceof StubConnector) return;
		Logger.debug("Downloading subjects from datahub...");
		long start = java.lang.System.currentTimeMillis();
		loadSubjectsFromDatahub(selector);
		long time = java.lang.System.currentTimeMillis() - start;
		Logger.debug("Datamart downloaded from datahub after " + time + " ms");
	}

	private void loadSubjectsFromDatahub(String selector) {
		Boolean localAccess = null;
		String[] subjects = datamartAccessor.listSubjectFiles(selector);
		for (String filename : subjects) {
			if (filename == null || filename.isBlank()) continue;
			try {
				File file = new File(filename);
				String id = file.getName().replace(".oss", "");
				String type = file.getParentFile().getName();
				SubjectNode subject = file.exists() ? new SubjectNode(this, id, type, file) : new SubjectNode(this, id, type, datamartAccessor);
				this.subjects.put(type, new HashMap<>());
				this.subjects.get(type).put(id, subject);
				if (localAccess == null) localAccess = file.exists();
				else localAccess &= file.exists();
			} catch (Exception e) {
				Logger.debug("Could not load subject " + filename + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}
		Logger.debug("Loaded " + subjects.length + " subjects (hasLocalAccess=" + this.localAccess + ")");
	}
}