package systems.intino.eventsourcing.datahubterminal.datamart;

import systems.intino.eventsourcing.datahubterminal.datamart.SubjectMounter.Operation;

public interface SubjectListener<T extends SubjectWrapper> {

	void onChange(T subject, Operation operation);
}
