package io.intino.ness.box.slack;

import io.intino.konos.slack.Bot;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.DatalakeManager;
import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.*;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

public class ManageSlack {

	private static final String OK = ":ok_hand:";
	private NessBox box;

	public ManageSlack(NessBox box) {
		this.box = box;
	}

	public void init(com.ullink.slack.simpleslackapi.SlackSession session) {
	}

	public String addUser(MessageProperties properties, String name, String[] groups) {
		String password = bus().addUser(name, groups.length == 0 ? Collections.emptyList() : asList(copyOfRange(groups, 1, groups.length)));
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

	public String removeUser(MessageProperties properties, String name) {
		return bus().removeUser(name) ? OK : "User not found";
	}

	public String addTank(MessageProperties properties, String tank) {
		NessGraph ness = ness();
		List<Tank> tanks = ness.tankList(t -> t.name$().equals(tank)).collect(toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		String name = tank.replaceFirst("feed\\.", "");
		Tank newTank = ness.create("tanks").tank(name);
		datalake().registerTank(newTank);
		datalake().feedFlow(newTank);
		newTank.save$();
		return OK;
	}

	public String removeTank(MessageProperties properties, String name) {
		NessGraph wrapper = ness();
		List<Tank> tanks = wrapper.tankList(t -> t.qualifiedName().equals(name)).collect(toList());
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			datalake().removeTank(tank);
			tank.delete$();
		}
		return OK;
	}

	public String addFunction(MessageProperties properties) {
		Bot.BotFile file = properties.file();
		String className = file.name().replace(".java", "");
		List<Function> functions = box.ness().functionList(f -> f.name$().equals(file.name())).collect(toList());
		if (!functions.isEmpty()) return "Function name is already defined";
		String sourceCode = file.textContent();
		if (sourceCode.isEmpty()) return "Code has errors or does not complies with MessageFunction interface";
		else {
			String result = box.datalakeManager().check(className, sourceCode);
			if (!result.isEmpty()) return result;
		}
		Function function = box.ness().create("functions", className).function(packageOf(sourceCode) + "." + className, sourceCode);
		function.save$();
		return OK + " ";
	}

	private String packageOf(String sourceCode) {
		return sourceCode.substring(0, sourceCode.indexOf("\n")).replaceAll("package |;", "");
	}

	public String removeFunction(MessageProperties properties, String name) {
		List<Function> functions = ness().functionList(t -> t.name$().equals(name)).collect(toList());
		if (functions.isEmpty()) return "Function not found";
		for (Function function : functions) {
			datalake().removeFunction(function);
			function.delete$();
		}
		return OK;
	}

	public Object addExternalBus(MessageProperties properties, String name, String url, String user, String password) {
		ExternalBus bus = box.ness().externalBusList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (bus != null) return "External Bus is already defined";
		box.ness().create("external-buses", name).externalBus(url.replaceAll("<|>", ""), user, password).save$();
		return OK;
	}

	public String removeExternalBus(MessageProperties properties, String name) {
		List<ExternalBus> buses = ness().externalBusList(t -> t.name$().equals(name)).collect(toList());
		if (buses.isEmpty()) return "External bus not found";
		for (ExternalBus externalBus : buses) {
			datalake().removeExternalBus(externalBus);
			externalBus.delete$();
		}
		return OK;
	}

	public String addAqueduct(MessageProperties properties, String name, String externalBus, String direction, String functionName, String tankMacro) {
		ExternalBus bus = box.ness().externalBusList(f -> f.name$().equals(externalBus)).findFirst().orElse(null);
		if (bus == null) return "External Bus not found";
		Function function = box.ness().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		if (function == null) return "Function not found";
		Aqueduct aqueduct = box.ness().aqueductList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct != null) return "Aqueduct is already defined";
		box.ness().create("aqueducts", name).aqueduct(Aqueduct.Direction.valueOf(direction), bus, function, tankMacro).save$();
		return OK;
	}

	public Object removeAqueduct(MessageProperties properties, String name) {
		List<Aqueduct> aqueducts = ness().aqueductList(t -> t.name$().equals(name)).collect(toList());
		if (aqueducts.isEmpty()) return "Aqueduct not found";
		for (Aqueduct tank : aqueducts) {
			tank.delete$();
		}
		return OK;
	}

	private NessGraph ness() {
		return box.ness();
	}

	private DatalakeManager datalake() {
		return box.datalakeManager();
	}

	private BusManager bus() {
		return box.busManager();
	}
}