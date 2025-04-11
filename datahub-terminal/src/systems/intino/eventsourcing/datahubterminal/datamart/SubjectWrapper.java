package systems.intino.eventsourcing.datahubterminal.datamart;

import io.intino.alexandria.logger.Logger;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Map;

public abstract class SubjectWrapper {

	protected final SubjectNode core;

	public SubjectWrapper(SubjectNode core) {
		this.core = core;
	}

	public String id() {
		return core.id();
	}

	public boolean enabled() {
		return core.enabled();
	}

	public void update$(Instant ts, String ss, Map<String, Object> attributes) {
		core.update(ts, ss, attributes);
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
