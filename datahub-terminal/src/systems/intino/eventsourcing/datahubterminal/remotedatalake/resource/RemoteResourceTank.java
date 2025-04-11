package systems.intino.eventsourcing.datahubterminal.remotedatalake.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.intino.alexandria.Scale;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.event.resource.ResourceEvent;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RemoteResourceTank implements Datalake.Store.Tank<ResourceEvent> {
	private final DatalakeAccessor accessor;
	private final JsonObject tank;
	private final JsonArray sources;

	public RemoteResourceTank(DatalakeAccessor accessor, JsonObject tank) {
		this.accessor = accessor;
		this.tank = tank;
		this.sources = tank.get("sources").getAsJsonArray();
	}

	@Override
	public String name() {
		return tank.get("name").getAsString();
	}

	@Override
	public Scale scale() {
		return Scale.valueOf(tank.get("scale").getAsString());
	}

	@Override
	public Datalake.Store.Source<ResourceEvent> source(String name) {
		return null;
	}

	@Override
	public Stream<Datalake.Store.Source<ResourceEvent>> sources() {
		return StreamSupport
				.stream(sources.spliterator(), true)
				.map(s -> new RemoteResourceSource(accessor, name(), s.getAsJsonObject()));
	}
}