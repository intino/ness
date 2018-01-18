package io.intino.ness.datalake.reflow;

import java.util.ArrayList;
import java.util.List;

public abstract class Job {
	private Thread thread;
	private boolean running = true;
	private List<Runnable> onTerminate = new ArrayList<>();

	public Job() {
		this.thread = new Thread(runnable(), "ness job");
		this.thread.start();
	}

	public final Thread thread() {
		return thread;
	}

	public final void onTerminate(Runnable runnable) {
		this.onTerminate.add(runnable);
	}

	protected boolean init() {
		return true;
	}

	public final void stop() {
		running = false;
	}

	protected abstract boolean step();

	private Runnable runnable() {
		return () -> {
			if (init()) {
				while (running)
					running = step();
				terminate();
			}
		};
	}

	public void terminate() {
		onTerminate();
		if (onTerminate != null) onTerminate.forEach(Runnable::run);
	}

	protected void onTerminate() {

	}

}
