package io.intino.ness.triton.box.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.alexandria.slack.Bot.MessageProperties;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.box.Utils;
import io.intino.ness.triton.box.actions.StartJmsConnectorAction;
import io.intino.ness.triton.box.actions.StopJmsConnectorAction;
import io.intino.ness.triton.graph.JMSConnector;
import io.intino.ness.triton.graph.User;
import org.apache.activemq.network.jms.JmsConnector;

import java.util.List;

import static io.intino.ness.triton.box.Utils.findTank;
import static java.util.stream.Collectors.toList;

public class NessieSlack {
	private static final String OK = ":ok_hand:";
	private ServiceBox box;

	public NessieSlack(ServiceBox box) {
		this.box = box;
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
		for (User user : users) builder.append(user.name()).append("\n");
		String result = builder.toString();
		return result.isEmpty() ? "There aren't users registered" : result;
	}

	public String tanks(MessageProperties properties) {
		StringBuilder builder = new StringBuilder().append("Tanks: \n");
		List<Tank> tanks = Utils.sortedTanks(box.datalake()).collect(toList());
		for (int i = 0; i < tanks.size(); i++) {
			Tank tank = tanks.get(i);
			builder.append(i + 1).append(") ").append(tank.name());
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

	public String tank(MessageProperties properties, String name) {
		Tank tank = findTank(box.datalake(), name);
		if (tank == null) return "tank not found";
		properties.context().command("tank");
		properties.context().objects(name);
		return "Selected " + tank.name();
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

	public String pump(MessageProperties properties, String input, String output) {
		return "";
	}

	private boolean isRunning(JMSConnector JMSConnector) {
		JmsConnector jmsConnector = box.busService().jmsConnectors().stream().filter(j -> j.getName().equals(JMSConnector.name$())).findFirst().orElse(null);
		return jmsConnector.isConnected();
	}

	public String reflow(MessageProperties properties, String[] tanks) {
		return "";
	}
}