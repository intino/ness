package io.intino.ness.datalake.reflow;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.datalake.ReflowMessageInputStream;
import io.intino.ness.datalake.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.time.Instant;
import java.util.Map;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;


public class ReflowProcess {
	private static final String FLOW_PATH = "flow.ness.reflow";
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final int blockSize;
	private final ReflowMessageInputStream stream;
	private final Session session;
	private final TopicProducer producer;
	private int count = 0;

	ReflowProcess(Session session, Map<Tank, Map.Entry<Instant, Instant>> tanks, Integer blockSize) {
		this.session = session;
		this.blockSize = blockSize == 0 ? Integer.MAX_VALUE : blockSize;
		this.stream = new ReflowMessageInputStream(tanks);
		this.producer = createProducer();
	}

	void next() {
		for (int i = 0; i < blockSize; i++) {
			if (!stream.hasNext()) break;
			count++;
			producer.produce(createMessageFor(stream.next().toString()));
		}
		producer.produce(stream.hasNext() ? createEndBlockMessage(count) : createEndReflowMessage(count));
		commit();
		if (!stream.hasNext()) close();
	}

	private Message createEndBlockMessage(int count) {
		return createMessageFor("[endBlock]\ncount: " + count + "\n");
	}

	private Message createEndReflowMessage(int count) {
		return createMessageFor("[endReflow]\ncount: " + count + "\n");
	}

	private void close() {
		stream.close();
		try {
			session.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void commit() {
		try {
			session.commit();
			logger.info("Commited " + blockSize + " messages");
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);

		}
	}

	private TopicProducer createProducer() {
		try {
			return new TopicProducer(session, FLOW_PATH);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}