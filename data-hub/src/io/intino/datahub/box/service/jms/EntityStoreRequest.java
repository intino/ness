package io.intino.datahub.box.service.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.jms.MessageTranslator;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.messages.DownloadMasterMessage;
import io.intino.ness.master.messages.MasterMessage;
import io.intino.ness.master.serialization.MasterMapSerializer;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Message;
import java.util.Iterator;
import java.util.stream.Stream;

public class EntityStoreRequest {

	private final Master master;

	public EntityStoreRequest(DataHubBox box) {
		this.master = box.master();
	}

	public Stream<Message> accept(Message message) {
		Iterator<io.intino.alexandria.message.Message> it = MessageTranslator.toInlMessages(message);
		while(it.hasNext()) {
			io.intino.alexandria.message.Message m = it.next();
			if(m.is(MasterMessage.INL_TYPE) && DownloadMasterMessage.class.getSimpleName().equals(m.get("messageClass").asString())) {
				return downloadEntities(new DownloadMasterMessage(m));
			}
		}
		return Stream.empty();
	}

	private Stream<Message> downloadEntities(DownloadMasterMessage m) {
		try {
			MasterMapSerializer serializer = MasterMapSerializer.getDefault();
			byte[] bytes = serializer.serialize(master.masterMap());
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setBooleanProperty("error", false);
			message.setStringProperty("serializer", serializer.name());
			message.writeBytes(bytes);
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.of(errorMessage(e));
		}
	}

	private static Message errorMessage(Throwable e) {
		try {
			ActiveMQTextMessage m = new ActiveMQTextMessage();
			m.setBooleanProperty("error", true);
			m.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
			return m;
		} catch (Exception ex) {
			Logger.error(ex);
			return null;
		}
	}
}
