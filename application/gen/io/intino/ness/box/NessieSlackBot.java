package io.intino.ness.box;

import io.intino.ness.box.slack.*;

import io.intino.konos.slack.Bot;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NessieSlackBot extends Bot {
	private static Logger LOG = Logger.getGlobal();
	private NessieSlack nessieSlack;
	private ManageSlack manageSlack;
	private TankSlack tankSlack;

	public NessieSlackBot(NessBox box) {
		super(box.configuration().nessieConfiguration().token);
		nessieSlack = new NessieSlack(box);
		manageSlack = new ManageSlack(box);
		tankSlack = new TankSlack(box);

		add("help", java.util.Collections.emptyList(), java.util.Collections.emptyList(), "Show this help", (properties, args) -> {
			final java.util.Map<String, CommandInfo> context = this.commandsInfoByContext(contexts().get(properties.username()).command());
			StringBuilder builder = new StringBuilder();
			context.keySet().forEach((c) -> builder.append(formatCommand(c, context.get(c))).append("\n"));
			return builder.toString();
		});
		add("exit", java.util.Collections.emptyList(), java.util.Collections.emptyList(), "Exit from current directory", (properties, args) -> {
			final Context context = this.contexts().get(properties.username());
			if (context != null) {
				String command = context.command();
				this.contexts().get(properties.username()).command("");
				return command.isEmpty() ? "" : "Exited from " + command + " " + String.join(" ", java.util.Arrays.asList(context.getObjects()));
			}
			return "";
		});
		add("where", java.util.Collections.emptyList(), java.util.Collections.emptyList(), "Shows the current directory", (properties, args) -> {
			final Context context = this.contexts().get(properties.username());
			return context != null ? context : "root";
		});
		add("manage".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList("add-user", "remove-user", "add-tank", "remove-tank", "add-function", "remove-function", "add-external-bus", "remove-external-bus", "add-jmsConnector", "remove-jms-connector", "pipes"), "Enter in the management area to add/remove tanks or users and other management actions", (properties, args) -> nessieSlack.manage(properties));
		add("add-user".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Add user to the datalake", (properties, args) -> manageSlack.addUser(properties, args.length > 0 ? args[0] : ""));
		add("remove-user".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Remove user from ness service", (properties, args) -> manageSlack.removeUser(properties, args.length > 0 ? args[0] : ""));
		add("add-tank".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Creates a tank", (properties, args) -> manageSlack.addTank(properties, args.length > 0 ? args[0] : ""));
		add("remove-tank".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Removes registered tank", (properties, args) -> manageSlack.removeTank(properties, args.length > 0 ? args[0] : ""));
		add("add-function".toLowerCase(), "manage", java.util.Arrays.asList(), java.util.Arrays.asList(), "Creates a function associated to an input tank and output tank. It requires the file of the function", (properties, args) -> manageSlack.addFunction(properties));
		add("remove-function".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Removes a registered function", (properties, args) -> manageSlack.removeFunction(properties, args.length > 0 ? args[0] : ""));
		add("add-external-bus".toLowerCase(), "manage", java.util.Arrays.asList("name", "externalBusUrl", "user", "password"), java.util.Arrays.asList(), "Defines an external bus to interact with it using jms connectors", (properties, args) -> manageSlack.addExternalBus(properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : "", args.length > 3 ? args[3] : ""));
		add("remove-external-bus".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Removes a registed external buses", (properties, args) -> manageSlack.removeExternalBus(properties, args.length > 0 ? args[0] : ""));
		add("add-jmsConnector".toLowerCase(), "manage", java.util.Arrays.asList("name", "externalBus", "direction", "topics"), java.util.Arrays.asList(), "Creates a data flow between an external bus and ness. It is necesary to define de direction of the data flow (*incoming* or *outgoing*). Also it is posible to set a conversion function.", (properties, args) -> manageSlack.addJmsConnector(properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : "", args));
		add("remove-jms-connector".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Removes a registed jms connector", (properties, args) -> manageSlack.removeJmsConnector(properties, args.length > 0 ? args[0] : ""));
		add("pipes".toLowerCase(), "manage", java.util.Arrays.asList(), java.util.Arrays.asList(), "Connects source and destination topics", (properties, args) -> manageSlack.pipes(properties));
		add("users".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "Request all users registered in ness", (properties, args) -> nessieSlack.users(properties));
		add("tanks".toLowerCase(), "", java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Request all tanks nessy is subscribed filtering by tags", (properties, args) -> nessieSlack.tanks(properties, args));
		add("functions".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "Show all functions registered", (properties, args) -> nessieSlack.functions(properties));
		add("topics".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "Show registered topics", (properties, args) -> nessieSlack.topics(properties));
		add("tank".toLowerCase(), "", java.util.Arrays.asList("name"), java.util.Arrays.asList("tag", "rename", "seal"), "Select a tank to operate with", (properties, args) -> nessieSlack.tank(properties, args.length > 0 ? args[0] : ""));
		add("tag".toLowerCase(), "tank", java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Tags thes tank with `tags`", (properties, args) -> tankSlack.tag(properties, args));
		add("rename".toLowerCase(), "tank", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Changes name of this tank to new one", (properties, args) -> tankSlack.rename(properties, args.length > 0 ? args[0] : ""));
		add("seal".toLowerCase(), "tank", java.util.Arrays.asList(), java.util.Arrays.asList(), "Seals current events of a tank to reservoir", (properties, args) -> tankSlack.seal(properties));
		add("pump".toLowerCase(), "", java.util.Arrays.asList("functionName", "input", "output"), java.util.Arrays.asList(), "Connect a source and destination tanks through a `function`", (properties, args) -> nessieSlack.pump(properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
		add("reflow".toLowerCase(), "", java.util.Arrays.asList("tanks"), java.util.Arrays.asList(), "Reproduce events of the tanks given. 'all' word will reflow all tanks", (properties, args) -> nessieSlack.reflow(properties, args));
		add("resume-tank".toLowerCase(), "", java.util.Arrays.asList("tank"), java.util.Arrays.asList(), "connect feed and flow channes of a tank", (properties, args) -> nessieSlack.resumeTank(properties, args.length > 0 ? args[0] : ""));
		add("pause-tank".toLowerCase(), "", java.util.Arrays.asList("tank"), java.util.Arrays.asList(), "stops the feed and flow channes of a tank", (properties, args) -> nessieSlack.pauseTank(properties, args.length > 0 ? args[0] : ""));
		add("start-jms-connector".toLowerCase(), "", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "connect out jms topic with a ness tank", (properties, args) -> nessieSlack.startJmsConnector(properties, args.length > 0 ? args[0] : ""));
		add("stop-jms-connector".toLowerCase(), "", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "connect out jms topic with a ness tank", (properties, args) -> nessieSlack.stopJmsConnector(properties, args.length > 0 ? args[0] : ""));
		add("jms-connectors".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "list of jms-connectors and its status", (properties, args) -> nessieSlack.jmsConnectors(properties));
		try {
			execute();
			this.nessieSlack.init(session());
			manageSlack.init(session());
			tankSlack.init(session());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static String formatCommand(String command, CommandInfo info) {
		return "`" + command.substring(command.lastIndexOf("$") + 1) + helpParameters(info.parameters()) + "` " + info.description() + "\n";
	}

	private static String helpParameters(java.util.List<String> parameters) {
		return parameters.isEmpty() ? "" : " <" + String.join("> <", parameters) + ">";
	}
}