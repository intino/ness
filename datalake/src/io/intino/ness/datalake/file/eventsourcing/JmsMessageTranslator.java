package io.intino.ness.datalake.file.eventsourcing;


import io.intino.alexandria.inl.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;

import javax.jms.TextMessage;

public class JmsMessageTranslator {

	public static Message toInlMessage(javax.jms.Message message) {
		try {
			return new ZimReader(((TextMessage) message).getText()).next();
		} catch (Throwable e) {
			Logger.error(e);
			return null;
		}
	}
}
