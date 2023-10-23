package org.example.test;

import io.intino.alexandria.Resource;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.core.Box;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.resource.ResourceEvent;
import io.intino.alexandria.event.resource.ResourceEventWriter;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.DataHubConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;
import io.intino.magritte.framework.stores.FileSystemStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Server {

	private static final String[] stashes = {"Solution"};

	public static void main(String[] args) throws IOException {
//		normalizeTimelineExtensions();

		File file = new File("temp/datalake/resources/Dictionary/default/00000101.zip");
		file.getParentFile().mkdirs();
		try(ResourceEventWriter writer = new ResourceEventWriter(file)) {
			ResourceEvent e = new ResourceEvent("Dictionary", "default", new Resource("default.dictionary.triplets", "Brasil\ten\tBrazil".getBytes(StandardCharsets.UTF_8)));
			e.ts(Timetag.of("00000101").instant());
			writer.write(e);
		}

		File file1 = new File("temp/datalake/resources/Dictionary/tc/00000101.zip");
		file1.getParentFile().mkdirs();
		try(ResourceEventWriter writer = new ResourceEventWriter(file1)) {
			ResourceEvent e = new ResourceEvent("Dictionary", "tc", new Resource("tc.dictionary.triplets", "Brasil\ten\tBrazil_1234\nSpain\tes\tEspaña".getBytes(StandardCharsets.UTF_8)));
			e.ts(Timetag.of("00000101").instant());
			writer.write(e);
		}

		DataHubConfiguration conf = new DataHubConfiguration(arguments());
		NessGraph graph = new Graph().loadStashes(stashes).as(NessGraph.class);
		loadUsers(conf.home(), graph);
		Box box = new DataHubBox(conf).put(graph.core$()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void normalizeTimelineExtensions() {
		File directory = new File("C:\\Users\\naits\\Desktop\\IntinoDev\\ness\\temp\\datahub\\datamarts\\master\\timelines");
		if(!directory.exists()) return;
		var files = FileUtils.listFiles(directory, new String[]{"tl"}, true);
		for(File file : files) {
			file.renameTo(new File(file.getAbsolutePath().replace(".tl", ".timeline")));
		}
	}

	private static void loadUsers(File workspace, NessGraph nessGraph) {
		try {
			nessGraph.broker().create().user("test", "test");
			nessGraph.broker().create().user("test2", "test2");
			nessGraph.broker().create().user("test3", "test3");
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static String[] arguments() throws IOException {
		File home = new File("./temp/test");
		if(home.exists()) FileUtils.deleteDirectory(home);
		home.mkdirs();
		return new String[] {
				"home=./temp/",
				"datalake_directory=./temp/datalake",
				"broker_port=63000",
				"broker_secondary_port=1882",
				"backup_directory=./temp/backup",
				"ui_port=9020"
		};
	}
}
