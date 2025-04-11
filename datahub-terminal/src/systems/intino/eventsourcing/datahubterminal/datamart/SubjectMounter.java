package systems.intino.eventsourcing.datahubterminal.datamart;

import systems.intino.eventsourcing.event.Event;

public interface SubjectMounter {

	void mount(Event event);

	SubjectMounter useListeners(boolean useListeners);


	enum Operation {
		Create, Update, Remove, Skip
	}
}