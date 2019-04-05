package io.intino.ness.tcp;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;
import io.intino.alexandria.zim.ZimStream;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;

import javax.jms.JMSException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static io.intino.ness.core.Session.Type.event;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class ActiveMQDatalake implements Datalake {
	private final Connection connection;
	private final ActiveMQSetStore setStore;
	private ActiveMQEventStore eventStore;

	public ActiveMQDatalake(String uri, String username, String password, String clientId) {
		this.connection = new Connection(uri, username, password, clientId, onOpen(), onClose());
		this.eventStore = new ActiveMQEventStore(connection);
		this.setStore = new ActiveMQSetStore(connection);
	}

	@Override
	public Datalake.Connection connection() {
		return connection;
	}

	@Override
	public EventStore eventStore() {
		return eventStore;
	}

	@Override
	public SetStore setStore() {
		return setStore;
	}

	@Override
	public void seal() {
		this.eventStore.seal();
	}

	@Override
	public void push(Stream<Session> sessions) {
		sessions.filter(b -> b.type().equals(event)).forEach(b -> eventStore.put(read(b), b.name()));
	}

	private CallBack onOpen() {
		return () -> {
			eventStore.open();
			setStore.open();
		};
	}

	private CallBack onClose() {
		return () -> eventStore.producers().forEach(p -> {
			if (!p.isClosed()) p.close();
		});
	}

	private ZimStream read(Session session) {
		try {
			return new ZimReader(new GZIPInputStream(session.inputStream()));
		} catch (IOException e) {
			Logger.error(e);
			return new ZimReader(new ByteArrayInputStream(new byte[0]));
		}
	}

	private interface CallBack {
		void execute();
	}

	public static class Connection implements Datalake.Connection {
		private final String uri;
		private final String username;
		private final String password;
		private final String clientId;
		private final CallBack onOpen;
		private final ActiveMQDatalake.CallBack onClose;
		private javax.jms.Session session;
		private String[] args;

		Connection(String uri, String username, String password, String clientId, CallBack onOpen, CallBack onClose) {
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

		private javax.jms.Session createSession(String arg) {
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

		public javax.jms.Session session() {
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
	}
}