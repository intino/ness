package io.intino.ness.box;

import io.intino.ness.box.actions.AddFunctionAction;
import io.intino.ness.box.actions.AddPipeAction;
import io.intino.ness.box.actions.AddTankAction;
import io.intino.ness.box.actions.AddUserAction;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainHsMapper {

	public static void main(String[] args) {
		args = new String[]{
				"ness_store=$HOME/.ness/store",
				"ness_datalake=$HOME/.ness/datalake",
				"broker_store=$HOME/.ness/broker/",
				"broker_port=63000",
				"mqtt_port=1884"
		};
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		Graph graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness");
		NessBox box = (NessBox) new NessBox(boxConfiguration).put(graph).open();
//		addUser(box, "happysense-server");
//		addTank(box, "dialogs.v3");
//		addTank(box, "dialogs.v4");
//		addTank(box, "surveys.v4");
//		addFunction(box, "DialogMapper", dialogMapperCode());
//		adddPipe(box, "feed.dialogs.v3", "feed.dialogs.v4", "DialogMapper");
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static void addUser(NessBox box, String name) {
		AddUserAction addUserAction = new AddUserAction();
		addUserAction.box = box;
		addUserAction.name = name;
		System.out.println(addUserAction.execute());
	}

	private static String dialogMapperCode() {
		try {
			List<String> strings = Files.readAllLines(new File("DialogMapper.java").toPath());
			return String.join("\n", strings);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void addTank(NessBox box, String tank) {
		AddTankAction addTankAction = new AddTankAction();
		addTankAction.box = box;
		addTankAction.name = tank;
		System.out.println(addTankAction.execute());
	}


	private static void addFunction(NessBox box, String name, String code) {
		AddFunctionAction addFunction = new AddFunctionAction();
		addFunction.box = box;
		addFunction.name = name;
		addFunction.code = code;
		System.out.println(addFunction.execute());
	}

	private static void adddPipe(NessBox box, String from, String to, String functionName) {
		AddPipeAction pipeAction = new AddPipeAction();
		pipeAction.box = box;
		pipeAction.from = from;
		pipeAction.to = to;
		pipeAction.functionName = functionName;
		System.out.println(pipeAction.execute());
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}

}
