package io.intino.datahub.service.jms;


import io.intino.alexandria.jms.MessageReader;
import io.intino.datahub.DataHub;

public class NessService {

	public NessService(DataHub hub) {
		hub.brokerService().manager().registerQueueConsumer("service.ness.seal", m -> new SealRequest(hub).accept(MessageReader.textFrom(m)));
		hub.brokerService().manager().registerQueueConsumer("service.ness.backup", m -> new BackupRequest(hub).accept(MessageReader.textFrom(m)));
	}
}