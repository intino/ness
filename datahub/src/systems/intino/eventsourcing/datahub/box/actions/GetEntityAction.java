package systems.intino.eventsourcing.datahub.box.actions;

import systems.intino.alexandria.datamarts.SubjectStore;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;

import static java.util.stream.Collectors.joining;


public class GetEntityAction {
	public systems.intino.eventsourcing.datahub.box.DatahubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String id;

	public String execute() {
		for (MasterDatamart d : box.datamarts().datamarts()) {
			SubjectStore store = d.subjectsStore().get(id);
			if (store != null)
				return store.tags().stream()
						.map(t -> t + ": " + store.categoricalQuery(t).current().value())
						.collect(joining("\n"));
		}
		return "null";
	}
}