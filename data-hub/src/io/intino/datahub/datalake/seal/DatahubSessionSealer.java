package io.intino.datahub.datalake.seal;

import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.SessionSealer;

import java.io.File;
import java.util.function.Predicate;

public class DatahubSessionSealer implements SessionSealer {
	private final Datalake datalake;
	private final io.intino.datahub.model.Datalake graphDl;
	private final File stageDir;
	private final File treatedDir;

	public DatahubSessionSealer(Datalake datalake, io.intino.datahub.model.Datalake graphDl, File stageDir, File treatedDir) {
		this.datalake = datalake;
		this.graphDl = graphDl;
		this.stageDir = stageDir;
		this.treatedDir = treatedDir;
	}

	@Override
	public synchronized void seal(Predicate<Datalake.Store.Tank<? extends Event>> sortingPolicy) {
		try {
			sealEvents(sortingPolicy);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	private void sealEvents(Predicate<Datalake.Store.Tank<? extends Event>> sortingPolicy) {
		new EventSessionSealer(datalake, graphDl, stageDir, tempDir(), treatedDir).seal(t -> check(t, sortingPolicy));
	}

	private boolean check(String tank, Predicate<Datalake.Store.Tank<? extends Event>> sortingPolicy) {
		return sortingPolicy.test(datalake.messageStore().tank(tank))
				|| sortingPolicy.test(datalake.measurementStore().tank(tank));
	}

	private File tempDir() {
		File temp = new File(stageDir, "temp");
		temp.mkdir();
		return temp;
	}
}
