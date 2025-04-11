package systems.intino.eventsourcing.datahub.datamart.mounters;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.message.Message;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DatamartMounter {
	protected final MasterDatamart datamart;

	public DatamartMounter(MasterDatamart datamart) {
		this.datamart = datamart;
	}

	public DatahubBox box() {
		return datamart.box();
	}

	public abstract void mount(MessageEvent event);

	public abstract void mount(Message message);

	public abstract Collection<String> destinationsOf(Message message);

	public Set<String> destinationsOf(Collection<Message> messages) {
		return messages.stream().flatMap(message -> destinationsOf(message).stream()).collect(Collectors.toSet());
	}
}
