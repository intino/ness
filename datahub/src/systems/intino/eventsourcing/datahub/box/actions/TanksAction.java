package systems.intino.eventsourcing.datahub.box.actions;

import systems.intino.eventsourcing.datalake.Datalake;

import static java.util.stream.Collectors.joining;


public class TanksAction {
	public systems.intino.eventsourcing.datahub.box.DatahubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public java.lang.String execute() {
		String collect = box.datalake().messageStore().tanks().map(Datalake.Store.Tank::name).collect(joining("\n"));
		return collect.isEmpty() ? "No tanks yet" : collect;
	}
}