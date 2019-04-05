package io.intino.ness.tcp;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;

import javax.jms.JMSException;
import javax.jms.Session;

public class AdminService extends Service {

	public AdminService(Session session) {
		super(session);
	}

	protected TopicProducer newProducer() {
		try {
			String ADMIN_PATH = "service.ness.admin";
			return new TopicProducer(session, ADMIN_PATH);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}
}
