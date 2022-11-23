package io.intino.ness.master.core;

public class MasterLifecycleEvent {

	private final State state;

	public MasterLifecycleEvent(State state) {
		this.state = state;
	}

	public State state() {
		return state;
	}

	@Override
	public String toString() {
		return "MasterLifecycleEvent[" +
				"state=" + state +
				']';
	}

	public enum State {
		STARTING,
		STARTED,
		SHUTTING_DOWN,
		SHUTDOWN,
		MERGING,
		MERGED,
		MERGE_FAILED,
		CLIENT_CONNECTED,
		CLIENT_DISCONNECTED,
		CLIENT_CHANGED_CLUSTER;
	}
}
