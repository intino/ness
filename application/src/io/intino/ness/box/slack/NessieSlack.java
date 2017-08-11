package io.intino.ness.box.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.PumpAction;
import io.intino.ness.box.actions.ReflowAction;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class NessieSlack {
	private static final String OK = ":ok_hand:";
	private NessBox box;

	public NessieSlack(NessBox box) {
		this.box = box;
	}

	public static String shortName(String qualifiedName) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
	}

	public void init(SlackSession session) {

	}

	public String manage(MessageProperties properties) {
		properties.context().command("manage");
		return "Now you are in management area";
	}

	public String users(MessageProperties properties) {
		Map<String, List<String>> users = box.busManager().users();
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
		List<Tank> tanks = Helper.sortedTanks(box.ness()).collect(toList());
		for (int i = 0; i < tanks.size(); i++) {
			Tank tank = tanks.get(i);
			if (tags.length == 0 || Helper.isTagged(tags, tank.tags()))
				builder.append(i + 1).append(") ").append(tank.qualifiedName());
			if (!tank.tags().isEmpty()) builder.append(" {").append(String.join(" ", tank.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "There aren't tanks" : value;
	}

	public String topics(MessageProperties properties) {
		List<String> topics = box.busManager().topicsInfo();
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
			builder.append(i).append(") ").append(functions.get(i).name$()).append(". Being used on:...TODO\n");
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


	public String startAqueduct(MessageProperties properties, String name) {
		Aqueduct aqueduct = box.ness().aqueductList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct == null) return "Aqueduct not found";
		box.datalakeManager().startAqueduct(aqueduct);
		return OK;
	}

	public String stopAqueduct(MessageProperties properties, String name) {
		Aqueduct aqueduct = box.ness().aqueductList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct == null) return "Aqueduct not found";
		box.datalakeManager().stopAqueduct(aqueduct);
		return OK;
	}

	public String aqueducts(MessageProperties properties) {
		List<Aqueduct> aqueducts = box.ness().aqueductList();
		if (aqueducts.isEmpty()) return "There aren't aqueducts registered";
		StringBuilder builder = new StringBuilder();
		for (Aqueduct aqueduct : aqueducts)
			builder.append(aqueduct.name$()).append(": ").append(box.datalakeManager().status(aqueduct) ? " started" : " stopped").append("\n");
		return builder.toString();
	}

	public String pump(MessageProperties properties, String functionName, String input, String output) {
		PumpAction action = new PumpAction();
		action.box = box;
		action.functionName = functionName;
		action.input = input;
		action.output = output;
		action.execute();
		return OK;
	}

	public String reflow(MessageProperties properties, String[] tanks) {
		ReflowAction action = new ReflowAction();
		action.box = box;
		action.tanks = !tanks[0].equalsIgnoreCase("all") ? Arrays.asList(tanks) : box.ness().tankList().stream().map(Tank::qualifiedName).collect(toList());
		return action.execute();
	}

	public String startFeedflow(MessageProperties properties, String tank) {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		box.datalakeManager().feed(aTank);
		return OK;
	}

	public String stopFeedflow(MessageProperties properties, String tank) {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		box.datalakeManager().stopFeed(aTank);
		return OK;
	}
}