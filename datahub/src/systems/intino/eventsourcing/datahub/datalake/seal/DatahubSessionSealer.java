package systems.intino.eventsourcing.datahub.datalake.seal;

import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.sealing.SessionSealer;

import java.io.File;

public class DatahubSessionSealer implements SessionSealer {
	private final Datalake datalake;
	private final systems.intino.eventsourcing.datahub.model.Datalake graphDl;
	private final File stageDir;
	private final File treatedDir;

	public DatahubSessionSealer(Datalake datalake, systems.intino.eventsourcing.datahub.model.Datalake graphDl, File stageDir, File treatedDir) {
		this.datalake = datalake;
		this.graphDl = graphDl;
		this.stageDir = stageDir;
		this.treatedDir = treatedDir;
	}

	@Override
	public synchronized void seal(TankFilter tankFilter) {
		try {
			treatedDir.mkdirs();
			sealEvents(tankFilter);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	private void sealEvents(TankFilter tankFilter) {
		new EventSessionSealer(datalake, graphDl, stageDir, tempDir(), treatedDir).seal(t -> tankFilter.test(datalake.messageStore().tank(t)));
	}

	private File tempDir() {
		File temp = new File(stageDir, "temp");
		temp.mkdir();
		return temp;
	}
}
