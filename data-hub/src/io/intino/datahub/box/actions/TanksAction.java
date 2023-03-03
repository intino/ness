package io.intino.datahub.box.actions;

import io.intino.alexandria.datalake.Datalake;
import io.intino.datahub.box.DataHubBox;

import static java.util.stream.Collectors.joining;


public class TanksAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public java.lang.String execute() {
		String collect = box.datalake().messageStore().tanks().map(Datalake.Store.Tank::name).collect(joining("\n"));
		return collect.isEmpty() ? "No tanks yet" : collect;
	}
}