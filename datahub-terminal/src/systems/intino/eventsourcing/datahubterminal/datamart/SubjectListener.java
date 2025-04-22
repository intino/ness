package systems.intino.eventsourcing.datahubterminal.datamart;

public interface SubjectListener<T extends SubjectWrapper> {
	enum Operation {
		Create, Update, Remove, Skip
	}
	void onChange(T subject, Operation operation);
}
