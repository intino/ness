package systems.intino.eventsourcing.datahubterminal.remotedatalake;

import com.google.gson.JsonArray;
import io.intino.alexandria.Json;
import jakarta.jms.Message;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.message.RemoteMessageTank;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.resource.RemoteResourceTank;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.event.resource.ResourceEvent;
import systems.intino.eventsourcing.jms.MessageReader;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RemoteDatalake implements Datalake {

	private final DatalakeAccessor accessor;

	public RemoteDatalake(JmsConnector connector) {
		accessor = new DatalakeAccessor(connector);
	}

	@Override
	public Store<MessageEvent> messageStore() {
		return new Store<>() {
			@Override
			public Stream<Tank<MessageEvent>> tanks() {
				Message response = accessor.query("messageStore/tanks");
				if (response == null) return Stream.empty();
				JsonArray content = Json.fromString(MessageReader.textFrom(response), JsonArray.class);
				return StreamSupport.stream(content.spliterator(), false).map(o -> new RemoteMessageTank(accessor, o.getAsJsonObject()));
			}

			@Override
			public Tank<MessageEvent> tank(String name) {
				return tanks().filter(t -> t.name().equals(name)).findFirst().orElse(null);
			}
		};
	}


	@Override
	public ResourceStore resourceStore() {
		return new ResourceStore() {
			@Override
			public Optional<ResourceEvent> find(ResourceEvent.REI rei) {
				return Optional.empty();
			}

			@Override
			public Stream<Tank<ResourceEvent>> tanks() {
				Message response = accessor.query("resourceStore/tanks");
				if (response == null) return Stream.empty();
				JsonArray content = Json.fromString(MessageReader.textFrom(response), JsonArray.class);
				return StreamSupport.stream(content.spliterator(), false).map(o -> new RemoteResourceTank(accessor, o.getAsJsonObject()));
			}

			@Override
			public Tank<ResourceEvent> tank(String name) {
				return tanks().filter(t -> t.name().equals(name)).findFirst().orElse(null);
			}
		};
	}
}
