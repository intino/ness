package io.intino.ness.master.core;

@FunctionalInterface
public interface MasterLifecycleListener {

	void onChanged(MasterLifecycleEvent event);
}
