package systems.intino.eventsourcing.datahubterminal.remotedatalake.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.intino.alexandria.Timetag;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor;
import systems.intino.eventsourcing.datalake.Datalake.Store.Source;
import systems.intino.eventsourcing.datalake.Datalake.Store.Tub;
import systems.intino.eventsourcing.event.resource.ResourceEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RemoteResourceSource implements Source<ResourceEvent> {
	private final DatalakeAccessor accessor;
	private final String tank;
	private final String source;
	private final List<String> tubs;

	public RemoteResourceSource(DatalakeAccessor accessor, String tank, JsonObject source) {
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
	public Tub<ResourceEvent> tub(Timetag timetag) {
		return on(timetag);
	}

	@Override
	public Stream<Tub<ResourceEvent>> tubs() {
		return tubs.stream()
				.map(t -> new RemoteResourceTub(accessor, tank, source, t));
	}

	@Override
	public Tub<ResourceEvent> first() {
		if (!tubs.isEmpty()) return new RemoteResourceTub(accessor, tank, source, tubs.get(0));
		return null;
	}

	@Override
	public Tub<ResourceEvent> last() {
		return !tubs.isEmpty() ? new RemoteResourceTub(accessor, tank, source, tubs.get(tubs.size() - 1)) : null;
	}

	@Override
	public Tub<ResourceEvent> on(Timetag timetag) {
		return tubs().filter(t -> t.timetag().equals(timetag)).findFirst().orElse(null);
	}

}
