package io.intino.ness.tcp;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

public class PumpService extends Service {

	private Map<String, String> queryResponseCache = new HashMap<>();

	public PumpService(Session session) {
		super(session);
	}

	@Override
	String request(String query) {
		if (queryResponseCache.containsKey(query)) return queryResponseCache.get(query);
		queryResponseCache.put(query, super.request(query));
		return queryResponseCache.get(query);
	}

	protected TopicProducer newProducer() {
		try {
			String ADMIN_PATH = "service.ness.reflow";
			return new TopicProducer(session, ADMIN_PATH);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}
}
