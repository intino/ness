package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import jakarta.jms.BytesMessage;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import systems.intino.alexandria.datamarts.SubjectStore;
import systems.intino.eventsourcing.datahubterminal.Connector;

import java.io.File;

public class DatamartAccessor {
	private static final String DATAHUB_MESSAGE_TOPIC = "service.ness.datamarts";
	private final String datamart;
	private final Connector connector;
	private final ConnectionConfig connectionConfig;
	private boolean localAccess;

	public DatamartAccessor(String datamart, Connector connector, ConnectionConfig connectionConfig) {
		this.datamart = datamart;
		this.connector = connector;
		this.connectionConfig = connectionConfig;
		this.localAccess = false;
	}

	public boolean localAccess() {
		return localAccess;
	}

	String[] listSubjectFiles(String selector) {
		try {
			ActiveMQTextMessage request = new ActiveMQTextMessage();
			request.setText("datamart=" + datamart + ";operation=list-subjects;" + ";sourceSelector=" + selector);
			jakarta.jms.Message message = requestResponseFromDatahub("list-subjects", request);
			if (!message.getBooleanProperty("success"))
				throw new Exception(((jakarta.jms.TextMessage) message).getText());
			String[] files = ((TextMessage) message).getText().split(",");
			if (files.length != 0 && new File(files[1]).exists()) this.localAccess = true;
			return files;
		} catch (Exception e) {
			Logger.error(e);
			return new String[0];
		}
	}

	SubjectStore getSubject(String id) throws Exception {
		jakarta.jms.Message response = requestResponseFromDatahub("get-subject=" + id, request(id, localAccess ? "path" : "download"));
		if (!response.getBooleanProperty("success")) throw new SubjectNotAvailableException(errorMessage(id));
		if (response instanceof jakarta.jms.TextMessage textResponse) {
			var file = getFile(textResponse);
			if (file != null && file.exists()) {
				localAccess = true;
				return new SubjectStore(file, id);
			}
			localAccess = false;
			response = requestResponseFromDatahub("get-subject=" + id, request(id, "download"));
		}
		if (!response.getBooleanProperty("success")) throw new SubjectNotAvailableException(errorMessage(id));
		return loadFromMessage((jakarta.jms.BytesMessage) response, id);
	}

	private jakarta.jms.Message requestResponseFromDatahub(String requestName, jakarta.jms.Message request) throws DatahubRequestException {
		long timeout = connectionConfig.initialTimeoutAmount;
		for (int i = 0; i < connectionConfig.maxAttempts; i++) {
			jakarta.jms.Message message = connector.requestResponse(DATAHUB_MESSAGE_TOPIC, request, timeout, connectionConfig.timeoutUnit);
			if (message != null) return message;
			if (i < connectionConfig.maxAttempts - 1)
				Logger.warn("(" + (i + 1) + ") Datahub did not respond after " + timeout + " " + connectionConfig.timeoutUnit + " to the request '" + requestName + "'. Trying again...");
			timeout *= (long) connectionConfig.timeoutMultiplier;
		}
		throw new DatahubRequestException("Datahub did not respond to the request '" + requestName + "' after " + connectionConfig.maxAttempts + " attempts.");
	}


	private SubjectStore loadFromMessage(BytesMessage m, String id) throws Exception {
		int size = m.getIntProperty("size");
		byte[] bytes = new byte[size];
		m.readBytes(bytes, size);
		var file = File.createTempFile(id, ".oss");
		java.nio.file.Files.write(file.toPath(), bytes, java.nio.file.StandardOpenOption.CREATE);
		file.deleteOnExit();
		localAccess = false;
		return new SubjectStore(file, id);
	}

	private static String errorMessage(String id) {
		return "Could not get subject " + id + " because datahub returned success=false in the response";
	}

	private jakarta.jms.Message request(String id, String mode) throws Exception {
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText("datamart=" + datamart + ";operation=get-subject;id=" + id + ";mode=" + mode);
		return message;
	}

	private File getFile(jakarta.jms.TextMessage m) {
		try {
			return new File(m.getText());
		} catch (Exception e) {
			return null;
		}
	}
}
