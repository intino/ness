package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datalake.regenerator.Mapper;
import io.intino.datahub.datalake.regenerator.MapperLoader;
import io.intino.datahub.datalake.regenerator.MapperReader;
import io.intino.datahub.datalake.regenerator.Regenerator;
import io.intino.datahub.graph.Datalake;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class ReviseAction {
	public DataHubBox box;
	public io.intino.alexandria.Context context = new io.intino.alexandria.Context();
	public String mapper;
	private static AtomicBoolean started = new AtomicBoolean(false);

	public String execute() {
		if (started.get()) return "Already started an action";
		started.set(true);
		try {
			String mapperCode = new MapperReader(box.mappersDirectory()).read(mapper);
			if (mapperCode == null) {
				started.set(false);
				return "Mapper not found";
			}
			Mapper mapper = new MapperLoader(box.configuration().home()).compileAndLoad(mapperCode);
			if (mapper == null) {
				started.set(false);
				return "Mapper " + this.mapper + " cannot be loaded";
			}
			Datalake.Backup backup = box.graph().datalake().backup();
			File sessionsDirectory = backup == null ? null : new File(backup.path(), "sessions");
			File reviewsDirectory = new File(box.configuration().home(), "reviews");
			reviewsDirectory.mkdirs();
			List<File> revise = new Regenerator(box.datalake(), sessionsDirectory, reviewsDirectory).revise(mapper);
			started.set(false);
			return Files.readString(revise.get(0).toPath());
		} catch (Exception e) {
			Logger.error(e);
			started.set(false);
			return "Impossible to create review: " + e.getMessage();
		}
	}
}