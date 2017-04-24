package io.intino.ness.konos.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.Tank;
import io.intino.ness.DatalakeManager;
import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.bus.BusManager;
import io.intino.ness.konos.NessBox;

import java.util.List;
import java.util.Map;

import static io.intino.ness.konos.slack.Helper.*;

public class NessieSlack {

	private static final String OK = ":ok_hand:";
	private NessBox box;

	public NessieSlack(NessBox box) {
		this.box = box;
	}

	public void init(SlackSession session) {

	}

	public String manage(MessageProperties properties) {
		properties.context().command("manage");
		return "Now you are in management area";
	}

	public String users(MessageProperties properties) {
		Map<String, List<String>> users = box.get(BusManager.class).users();
		StringBuilder builder = new StringBuilder();
		for (String user : users.keySet()) {
			builder.append(user);
			List<String> groups = users.get(user);
			if (!groups.isEmpty()) builder.append(" {").append(String.join(" ", groups)).append("}");
			builder.append("\n");
		}
		String result = builder.toString();
		return result.isEmpty() ? "There aren't users registered" : result;
	}

	public String tanks(MessageProperties properties, String[] tags) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Tank> tank = ness.tankList();
		for (int i = 0; i < tank.size(); i++) {
			Tank topic = tank.get(i);
			if (tags.length == 0 || isTagged(tags, topic.tags()))
				builder.append(i + 1).append(") ").append(topic.qualifiedName());
			if (!topic.tags().isEmpty()) builder.append(" {").append(String.join(" ", topic.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "No tank" : value;
	}

	public String functions(MessageProperties properties) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Function> functions = ness.functionList();
		for (int i = 0; i < functions.size(); i++)
			builder.append(i).append(") ").append(functions.get(i).name()).append(". Being used on:...\n");
		String value = builder.toString();
		return value.isEmpty() ? "No functions" : value;
	}

	public String tank(MessageProperties properties, String name) {
		Tank tank = findTank(box, name);
		if (tank == null) return "tank not found";
		properties.context().command("tank");
		properties.context().objects(name);
		return "Selected " + tank.qualifiedName();
	}

	public String addFunction(MessageProperties properties, String name, String code) {
		Ness ness = ness(box);
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name().equals(name));
		if (!functions.isEmpty()) return "function name is already defined";
		if (!datalake().isCorrect(code)) return "Code has errors or does not complies with NessFunction interface";
		Function function = ness.create("functions", name).function(sourceCode);
		function.save();
		return OK;
	}

	public String pump(MessageProperties properties, String functionName, String input, String output) {
		Ness ness = ness(box);
		List<Function> functions = ness.functionList(f -> f.name().equals(functionName));
		if (functions.isEmpty()) return "Function not found";
		Function function = functions.get(0);
		datalake().pump(function, input, output);
		return OK;
	}

	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}

	public String reflow(MessageProperties properties, String tankName) {
		Tank tank = findTank(box, tankName);
		if (tank == null) return "Channel not found";
		DatalakeManager manager = datalake();
		manager.reflow(tank);
		return OK;
	}

	private String nextVersionOf(Tank tank) {
		return tank.name().replace("." + tank.version(), "." + tank.version() + 1);
	}

	public String migrate(MessageProperties properties, String tankName, String[] args) {
		Tank tank = findTank(box, tankName);
		String newChannelName = nextVersionOf(tank);
		Tank newChannel = ness(box).create("tanks", newChannelName).tank(newChannelName);
		datalake().migrate(tank, newChannel);
		return OK;
	}
}