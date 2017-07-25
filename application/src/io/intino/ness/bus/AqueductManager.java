package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.inl.TextMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.logging.Logger;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class AqueductManager {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AqueductManager.class);
	private static final String ID = "aqueduct";

	private final Aqueduct aqueduct;
	private final Session ness;
	private final Session origin;
	private final TextMapper function;
	private TopicConsumer topicConsumer;
	private Session session;


	public AqueductManager(Aqueduct aqueduct, Session nessSession) {
		this.aqueduct = aqueduct;
		this.ness = nessSession;
		this.origin = initOriginSession();
		this.function = map(aqueduct.transformer().qualifiedName(), aqueduct.transformer().source());
	}

	public void start() {
		topicConsumer = new TopicConsumer(origin, aqueduct.originTopic());
		topicConsumer.listen(message -> consumeFeed(ness, aqueduct.destinationTopic(), message));
	}

	public void stop() {
		try {
			if (topicConsumer != null) topicConsumer.stop();
			session.close();
		} catch (JMSException e) {
		}
	}

	private Session initOriginSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(aqueduct.originURL());
			javax.jms.Connection connection = connectionFactory.createConnection(aqueduct.user(), aqueduct.password());
			connection.setClientID(ID);
			session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			connection.start();
			return session;
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private void consumeFeed(Session destination, String topic, Message message) {
		try {
			new TopicProducer(destination, topic).produce(createMessageFor(mapToMessage(textFrom(message)).toString()));
		} catch (JMSException e) {
			Logger.getGlobal().severe(e.getMessage());
		}
	}

	private io.intino.ness.inl.Message mapToMessage(String message) {
		return function.map(message);
	}

	private Compiler.Result compile(String function, String... sources) {
		return Compiler.compile(sources)
				.with("-target", "1.8")
				.load(function);
	}

	private TextMapper map(String function, String... sources) {
		try {
			return map(compile(function, sources).as(TextMapper.class));
		} catch (Exception e) {
			return null;
		}
	}

	private TextMapper map(Class<? extends TextMapper> mapperClass) throws Exception {
		return mapperClass.newInstance();
	}

	private static String textFrom(Message message) {
		try {
			if (message instanceof BytesMessage) {
				byte[] data = new byte[(int) ((BytesMessage) message).getBodyLength()];
				((BytesMessage) message).readBytes(data);
				return new String(data);
			} else return ((TextMessage) message).getText();
		} catch (JMSException e) {
			e.printStackTrace();
			return "";
		}
	}
}
