package systems.intino.eventsourcing.datahubterminal.remotedatalake.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.intino.alexandria.Timetag;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor;
import systems.intino.eventsourcing.datalake.Datalake.Store.Source;
import systems.intino.eventsourcing.datalake.Datalake.Store.Tub;
import systems.intino.eventsourcing.event.message.MessageEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemoteMessageSource implements Source<MessageEvent> {
	private final DatalakeAccessor accessor;
	private final String tank;
	private final String source;
	private final List<String> tubs;

	public RemoteMessageSource(DatalakeAccessor accessor, String tank, JsonObject source) {
		this.accessor = accessor;
		this.tank = tank;
		this.source = source.get("name").getAsString();
		this.tubs = source.get("tubs").getAsJsonArray().asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
	}

	@Override
	public String name() {
		return source;
	}

	@Override
	public Tub<MessageEvent> tub(Timetag timetag) {
		return on(timetag);
	}

	@Override
	public Stream<Tub<MessageEvent>> tubs() {
		return tubs.stream()
				.map(t -> new RemoteMessageTub(accessor, tank, source, t));
	}

	@Override
	public Tub<MessageEvent> first() {
		if (!tubs.isEmpty()) return new RemoteMessageTub(accessor, tank, source, tubs.get(0));
		return null;
	}

	@Override
	public Tub<MessageEvent> last() {
		return !tubs.isEmpty() ? new RemoteMessageTub(accessor, tank, source, tubs.get(tubs.size() - 1)) : null;
	}

	@Override
	public Tub<MessageEvent> on(Timetag timetag) {
		return tubs().filter(t -> t.timetag().equals(timetag)).findFirst().orElse(null);
	}

}
