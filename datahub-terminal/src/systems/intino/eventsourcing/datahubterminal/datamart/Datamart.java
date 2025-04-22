package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.eventsourcing.datahubterminal.Connector;
import systems.intino.eventsourcing.datahubterminal.StubConnector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Datamart {
	private final String name;
	private final Connector connector;
	private final DatamartAccessor accessor;
	private Instant ts;
	protected final Map<String, List<SubjectListener<SubjectWrapper>>> subjectListeners = new HashMap<>();

	public Datamart(String name, Connector connector, ConnectionConfig connectionConfig) {
		this.name = name;
		this.connector = requireNonNull(connector);
		this.accessor = new DatamartAccessor(name, connector, connectionConfig);
	}

	public synchronized Datamart init(String datamartSourceSelector) {
		try {
			connectToDatamart(datamartSourceSelector);
			Logger.info("MasterDatamart (" + connector.clientId() + ") initialized successfully.");
		} catch (Exception e) {
			throw new ExceptionInInitializerError("MasterDatamart failed to start because a " + e.getClass().getName() + " occurred: " + e.getMessage());
		}
		return this;
	}

	public Instant ts() {
		return ts;
	}

	public int size() {
		return accessor.size();
	}

	public Subject getSubject(String name, String type) {
		return accessor.getSubject(name, type);
	}

	public Subject getSubject(String[] possibleTypes, String id) {
		return subjects(possibleTypes).filter(s -> s.name().equals(id)).findFirst().orElse(null);
	}

	public Stream<Subject> subjects() {
		return accessor.subjects();
	}

	public Stream<Subject> subjects(String... types) {
		return accessor.subjects(types);
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

	private void connectToDatamart(String selector) {
		if (connector instanceof StubConnector) return;
		Logger.debug("Connecting to " + name + " datamart...");
		boolean connected = accessor.connect();
		if (connected) Logger.debug("Connected to " + name + " datamart!");
	}

}