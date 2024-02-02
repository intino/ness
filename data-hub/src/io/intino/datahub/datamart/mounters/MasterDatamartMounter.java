package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class MasterDatamartMounter {

	protected final MasterDatamart datamart;

	public MasterDatamartMounter(MasterDatamart datamart) {
		this.datamart = datamart;
	}

	public DataHubBox box() {
		return datamart.box();
	}

	public abstract void mount(io.intino.alexandria.event.Event event);

	public abstract void mount(Message message);

	public abstract Collection<String> destinationsOf(Message message);

	public Collection<String> destinationsOf(Collection<Message> messages) {
		return messages.stream().flatMap(message -> destinationsOf(message).stream()).distinct().collect(Collectors.toSet());
	}


}
