package io.intino.ness.box.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.*;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.JMSConnector;
import io.intino.ness.graph.Tank;
import io.intino.ness.graph.User;
import org.apache.activemq.network.jms.JmsConnector;

import java.util.List;

import static io.intino.ness.box.slack.Helper.findTank;
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
		List<User> users = box.graph().userList();
		StringBuilder builder = new StringBuilder();
		for (User user : users) {
			builder.append(user.name$()).append("\n");
		}
		String result = builder.toString();
		return result.isEmpty() ? "There aren't users registered" : result;
	}

	public String tanks(MessageProperties properties, String[] tags) {
		StringBuilder builder = new StringBuilder().append("Tanks: \n");
		List<Tank> tanks = Helper.sortedTanks(box.graph()).collect(toList());
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
		List<Function> functions = box.graph().functionList();
		for (int i = 0; i < functions.size(); i++)
			builder.append(i).append(") ").append(functions.get(i).name$()).append(". Being used on:...TODO\n");
		String value = builder.toString();
		return value.isEmpty() ? "There aren't functions registered yet" : value;
	}

	public String tank(MessageProperties properties, String name) {
		Tank tank = findTank(box, name);
		if (tank == null) return "tank not found";
		properties.context().command("tank");
		properties.context().objects(name);
		return "Selected " + tank.qualifiedName();
	}


	public String startJmsConnector(MessageProperties properties, String name) {
		StartJmsConnectorAction action = new StartJmsConnectorAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String stopJmsConnector(MessageProperties properties, String name) {
		StopJmsConnectorAction action = new StopJmsConnectorAction();
		action.box = box;
		action.name = name;
		return action.execute();
	}

	public String jmsConnectors(MessageProperties properties) {
		List<JMSConnector> JMSConnectors = box.graph().jMSConnectorList();
		if (JMSConnectors.isEmpty()) return "There aren't bus pipes registered";
		StringBuilder builder = new StringBuilder();
		for (JMSConnector JMSConnector : JMSConnectors)
			builder.append(JMSConnector.name$()).append(": ").append(isRunning(JMSConnector) ? " started" : " stopped").append("\n");
		return builder.toString();
	}

	private boolean isRunning(JMSConnector JMSConnector) {
		JmsConnector jmsConnector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		return jmsConnector.isConnected();
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

	public String resumeTank(MessageProperties properties, String tank) {
		final ResumeTankAction action = new ResumeTankAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String pauseTank(MessageProperties properties, String tank) {
		PauseTankAction action = new PauseTankAction();
		action.box = box;
		action.tank = tank;
		return action.execute();
	}

	public String reflow(MessageProperties properties, String[] tanks) {
		return "";
	}
}