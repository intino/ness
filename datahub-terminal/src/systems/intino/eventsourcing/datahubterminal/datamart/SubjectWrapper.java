package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;
import systems.intino.datamarts.subjectstore.SubjectHistory.Current;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.lang.reflect.InvocationTargetException;

public abstract class SubjectWrapper {
	protected final Subject core;
	private final Current current;
	private final Datamart datamart;

	public SubjectWrapper(Subject core, Datamart datamart) {
		this.core = core;
		this.current = core.history().current();
		this.datamart = datamart;
	}

	public String id() {
		return core.name();
	}

	public boolean enabled() {
		return current.text("enable").equals("true");
	}

	protected Datamart datamart$() {
		return datamart;
	}

	protected Current current$() {
		return current;
	}

	public <T extends SubjectWrapper> T as$(Class<T> other) {
		try {
			return (T) other.getConstructors()[0].newInstance(core);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			Logger.error(e);
			return null;
		}
	}
}
