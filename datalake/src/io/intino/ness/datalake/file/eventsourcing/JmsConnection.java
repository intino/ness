package io.intino.ness.datalake.file.eventsourcing;

import io.intino.alexandria.logger.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;

import javax.jms.JMSException;
import javax.jms.Session;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;


public class JmsConnection implements EventSubscriber.Connection {
	private final String uri;
	private final String username;
	private final String password;
	private final String clientId;
	private final CallBack onOpen;
	private final CallBack onClose;
	private Session session;
	private String[] args;

	public JmsConnection(String uri, String username, String password, String clientId, CallBack onOpen, CallBack onClose) {
		this.uri = uri;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.onOpen = onOpen;
		this.onClose = onClose;
	}

	@Override
	public void connect(String... args) {
		this.args = args;
		this.session = createSession(args.length > 0 ? args[0] : "");
		this.onOpen.execute();
	}

	private Session createSession(String arg) {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri);
			javax.jms.Connection connection = connectionFactory.createConnection(username, password);
			if (clientId != null) connection.setClientID(clientId);
			connection.start();
			return connection.createSession(arg != null && arg.equals("Transacted"), AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	public Session session() {
		if (session == null || ((ActiveMQSession) session).isClosed()) connect(args);
		return session;
	}

	@Override
	public void disconnect() {
		if (session != null && !((ActiveMQSession) session).isClosed()) {
			try {
				onClose.execute();
				session.close();
			} catch (JMSException e) {
				Logger.error(e);
			}
		}
	}

	private interface CallBack {
		void execute();
	}
}
