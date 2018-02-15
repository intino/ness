package io.intino.ness.datalake.reflow;

import java.util.ArrayList;
import java.util.List;

public abstract class Job {
	private Thread thread;
	private boolean running = true;
	private List<Runnable> onTerminate = new ArrayList<>();

	Job() {
		this.thread = new Thread(runnable(), "ness job");
		this.thread.start();
	}

	public final void onTerminate(Runnable runnable) {
		this.onTerminate.add(runnable);
	}

	protected abstract boolean step();

	private Runnable runnable() {
		return () -> {
			while (running) running = step();
			terminate();
		};
	}

	private void terminate() {
		if (onTerminate != null) onTerminate.forEach(Runnable::run);
	}

}
