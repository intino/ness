package io.intino.datahub.box.service.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.jms.MessageTranslator;
import io.intino.ness.master.core.Master;
import io.intino.ness.master.messages.DownloadMasterMessage;
import io.intino.ness.master.messages.MasterMessage;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterMapSerializer;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Message;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.ness.master.messages.DownloadMasterMessage.EntityFilter.*;
import static io.intino.ness.master.messages.DownloadMasterMessage.*;

public class EntityStoreRequest {

	private final Master master;

	public EntityStoreRequest(DataHubBox box) {
		this.master = box.master();
	}

	public Stream<Message> accept(Message message) {
		Iterator<io.intino.alexandria.message.Message> it = MessageTranslator.toInlMessages(message);
		while(it.hasNext()) {
			io.intino.alexandria.message.Message m = it.next();
			if(m.is(MasterMessage.INL_TYPE) && DownloadMasterMessage.class.getName().equals(m.get("messageClass").asString())) {
				return downloadEntities(new DownloadMasterMessage(m));
			}
		}
		return Stream.empty();
	}

	private Stream<Message> downloadEntities(DownloadMasterMessage m) {
		try {
			MasterMapSerializer mapSerializer = MasterMapSerializer.getDefault();
			byte[] bytes = mapSerializer.serialize(getMasterMap(m));

			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setStringProperty(PROPERTY_ENTITY_SERIALIZER, master.serializer().name());
			message.setStringProperty(PROPERTY_MAP_SERIALIZER, mapSerializer.name());
			message.setBooleanProperty(PROPERTY_ERROR, false);
			message.setIntProperty(PROPERTY_BODY_SIZE, bytes.length);
			message.writeBytes(bytes);

			return Stream.of(message);

		} catch (Exception e) {
			Logger.error(e);
			return Stream.of(errorMessage(e));
		}
	}

	private Map<String, String> getMasterMap(DownloadMasterMessage m) {
		return master.masterMap().entrySet().parallelStream()
				.filter(entry -> matchesClientFilters(entry, m))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private boolean matchesClientFilters(Map.Entry<String, String> entry, DownloadMasterMessage m) {
		String key = entry.getKey();
		String value = entry.getValue();

		if(!m.tanks().contains(tankOf(key))) return false;
		if(m.filter() == AllEntities) return true;

		TripletRecord record = master.serializer().deserialize(value);
		boolean enabled = "true".equals(record.getValue("enabled"));

		return enabled && m.filter() == OnlyEnabled || !enabled && m.filter() == OnlyDisabled;
	}

	private String tankOf(String id) {
		return StringUtils.capitalize(Triplet.typeOf(id));
	}

	private static Message errorMessage(Throwable e) {
		try {
			ActiveMQTextMessage m = new ActiveMQTextMessage();
			m.setBooleanProperty(PROPERTY_ERROR, true);
			m.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
			return m;
		} catch (Exception ex) {
			Logger.error(ex);
			return null;
		}
	}
}
