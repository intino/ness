package io.intino.datahub.broker.jms;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.master.core.Master;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.File;
import java.util.function.Consumer;

class EntitySerializer {
	private final FileDatalake datalake;
	private final Master master;

	public EntitySerializer(FileDatalake datalake, Master master) {

		this.datalake = datalake;
		this.master = master;
	}

	Consumer<Message> create() {
		return message -> {
			try {
				save(((TextMessage)message).getText());
			} catch (JMSException e) {
				Logger.error(e);
			}
		};
	}

	private void save(String message) {
	}

}