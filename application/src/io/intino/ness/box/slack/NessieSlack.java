package io.intino.ness.box.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.box.NessBox;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

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
		Map<String, List<String>> users = box.datalakeManager().users();
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
		StringBuilder builder = new StringBuilder().append("Tanks: \n");
		List<Tank> tank = Helper.sortedTanks(box.ness()).collect(toList());
		for (int i = 0; i < tank.size(); i++) {
			Tank topic = tank.get(i);
			if (tags.length == 0 || Helper.isTagged(tags, topic.tags()))
				builder.append(i + 1).append(") ").append(topic.qualifiedName());
			if (!topic.tags().isEmpty()) builder.append(" {").append(String.join(" ", topic.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "There aren't tanks" : value;
	}

	public String topics(MessageProperties properties) {
		List<String> topics = box.datalakeManager().topics();
		StringBuilder builder = new StringBuilder();
		int i = 1;
		for (String topic : topics)
			builder.append(i++).append(") ").append(topic).append("\n");
		return builder.toString();
	}

	public String functions(MessageProperties properties) {
		StringBuilder builder = new StringBuilder();
		List<Function> functions = box.ness().functionList();
		for (int i = 0; i < functions.size(); i++)
			builder.append(i).append(") ").append(functions.get(i).name$()).append(". Being used on:...\n");
		String value = builder.toString();
		return value.isEmpty() ? "There aren't functions registered yet" : value;
	}

	public String tank(MessageProperties properties, String name) {
		Tank tank = Helper.findTank(box, name);
		if (tank == null) return "tank not found";
		properties.context().command("tank");
		properties.context().objects(name);
		return "Selected " + tank.qualifiedName();
	}

	public String addFunction(MessageProperties properties, String name, String code) {
		String sourceCode = Helper.downloadFile(code);
		List<Function> functions = box.ness().functionList(f -> f.name$().equals(name)).collect(toList());
		if (!functions.isEmpty()) return "function name is already defined";
		if (!box.datalakeManager().isCorrect(code)) return "Code has errors or does not complies with NessFunction interface";
		Function function = box.ness().create("functions", name).function(sourceCode);
		function.save$();
		return OK;
	}

	public String pump(MessageProperties properties, String functionName, String input, String output) {
		List<Function> functions = box.ness().functionList(f -> f.name$().equals(functionName)).collect(toList());
		if (functions.isEmpty()) return "Function not found";
		Function function = functions.get(0);
		box.datalakeManager().pump(function, input, output);
		return OK;
	}

}