package systems.intino.eventsourcing.datahub.box.actions;

import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;

import static java.util.stream.Collectors.joining;

public class GetSubjectAction {
	public String type;
	public String name;
	public systems.intino.eventsourcing.datahub.box.DatahubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();

	public String execute() {
		for (MasterDatamart d : box.datamarts().datamarts()) {
			Subject store = d.subjectsStore().get(name, type);
			if (store != null)
				return store.history().tags().stream()
						.map(t -> t + ":" + store.history().current().text(t))
						.collect(joining("\n"));
		}
		return "null";
	}
}