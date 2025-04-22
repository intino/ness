package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.eventsourcing.datahubterminal.Connector;

import java.util.Set;
import java.util.stream.Stream;

public class DatamartAccessor {
	private static final String DATAHUB_MESSAGE_TOPIC = "service.ness.datamarts";
	private final String datamart;
	private final Connector connector;
	private final ConnectionConfig connectionConfig;
	private SubjectStore store;

	public DatamartAccessor(String datamart, Connector connector, ConnectionConfig connectionConfig) {
		this.datamart = datamart;
		this.connector = connector;
		this.connectionConfig = connectionConfig;
	}

	public boolean connect() {
		try {
			ActiveMQTextMessage request = new ActiveMQTextMessage();
			request.setText("datamart=" + datamart + ";operation=source;");
			jakarta.jms.Message message = requestResponseFromDatahub(request);
			if (!message.getBooleanProperty("success"))
				throw new Exception(((jakarta.jms.TextMessage) message).getText());
			var source = ((TextMessage) message).getText();
			if (source != null) {
				this.store = new SubjectStore(source);
				return true;
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return false;
	}

	public int size() {
		return store.subjects().size();
	}

	Subject getSubject(String name, String type) {
		return store.open(name, type);
	}

	private jakarta.jms.Message requestResponseFromDatahub(Message request) throws DatahubRequestException {
		long timeout = connectionConfig.initialTimeoutAmount;
		for (int i = 0; i < connectionConfig.maxAttempts; i++) {
			jakarta.jms.Message message = connector.requestResponse(DATAHUB_MESSAGE_TOPIC, request, timeout, connectionConfig.timeoutUnit);
			if (message != null) return message;
			if (i < connectionConfig.maxAttempts - 1)
				Logger.warn("(" + (i + 1) + ") Datahub did not respond after " + timeout + " " + connectionConfig.timeoutUnit + " to the request '" + "source" + "'. Trying again...");
			timeout *= (long) connectionConfig.timeoutMultiplier;
		}
		throw new DatahubRequestException("Datahub did not respond to the request '" + "source" + "' after " + connectionConfig.maxAttempts + " attempts.");
	}

	public Stream<Subject> subjects() {
		return store.subjects().collect().stream();
	}

	public Stream<Subject> subjects(String... types) {
		Set<String> set = Set.of(types);
		return store.subjects().collect().stream().filter(s -> set.contains(s.type()));
	}
}
