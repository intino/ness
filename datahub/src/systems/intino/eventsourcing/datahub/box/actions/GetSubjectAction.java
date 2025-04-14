package systems.intino.eventsourcing.datahub.box.actions;

import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;

import static java.util.stream.Collectors.joining;


public class GetSubjectAction {
	public systems.intino.eventsourcing.datahub.box.DatahubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String id;

	public String execute() {
		for (MasterDatamart d : box.datamarts().datamarts()) {
			SubjectStore store = d.subjectsStore().get(id);
			if (store != null)
				return store.tags().stream()
						.map(t -> t + ": " + store.categoricalQuery(t).get().value())
						.collect(joining("\n"));
		}
		return "null";
	}
}