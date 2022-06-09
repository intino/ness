package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zip.Zip;
import io.intino.datahub.box.DataHubBox;

import java.io.File;
import java.io.IOException;
import java.time.Instant;


public class RestoreDatalakeAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String timetag;

	public String execute() {
		File root = box.datalake().root();
		root.renameTo(new File(root.getParentFile(), root.getName() + "_removed_" + instant()));
		File backupDirectory = box.configuration().backupDirectory();
		File origin = new File(backupDirectory, "datalake" + File.separator + timetag + ".zip");
		if (!origin.exists()) return "This timetag doesn't exist";
		try {
			new Zip(origin).unzip(root.getParentFile().getAbsolutePath());
			return "Datalake restored!";
		} catch (IOException e) {
			Logger.error(e);
			return e.getMessage();
		}
	}

	private String instant() {
		String instant = Instant.now().toString().replace(":", "-");
		return instant.substring(0, instant.indexOf("."));
	}
}