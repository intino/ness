package io.intino.ness.konos;

import io.intino.ness.konos.slack.*;

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
		add("manage".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList("add-user", "remove-user", "add-tank", "remove-tank"), "Enter in the management area to add/remove tanks or users and other management actions", (properties, args) -> nessieSlack.manage(properties));
		add("add-user".toLowerCase(), "manage", java.util.Arrays.asList("name", "groups"), java.util.Arrays.asList(), "Request all tanks nessy is subscribed filtered by tags", (properties, args) -> manageSlack.addUser(properties, args.length > 0 ? args[0] : "", args));
		add("remove-user".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Remove user from ness service", (properties, args) -> manageSlack.removeUser(properties, args.length > 0 ? args[0] : ""));
		add("add-tank".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Creates a tank", (properties, args) -> manageSlack.addTank(properties, args.length > 0 ? args[0] : ""));
		add("remove-tank".toLowerCase(), "manage", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Select a tank to operate with", (properties, args) -> manageSlack.removeTank(properties, args.length > 0 ? args[0] : ""));
		add("users".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "Request all users registered in ness", (properties, args) -> nessieSlack.users(properties));
		add("tanks".toLowerCase(), "", java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Request all tanks nessy is subscribed filtering by tags", (properties, args) -> nessieSlack.tanks(properties, args));
		add("functions".toLowerCase(), "", java.util.Arrays.asList(), java.util.Arrays.asList(), "Show all functions registered", (properties, args) -> nessieSlack.functions(properties));
		add("tank".toLowerCase(), "", java.util.Arrays.asList("name"), java.util.Arrays.asList("tag", "rename", "consolidate"), "Select a tank to operate with", (properties, args) -> nessieSlack.tank(properties, args.length > 0 ? args[0] : ""));
		add("tag".toLowerCase(), "tank", java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Tags thes tank with `tags`", (properties, args) -> tankSlack.tag(properties, args));
		add("rename".toLowerCase(), "tank", java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Changes name of this tank to new one", (properties, args) -> tankSlack.rename(properties, args.length > 0 ? args[0] : ""));
		add("consolidate".toLowerCase(), "tank", java.util.Arrays.asList(), java.util.Arrays.asList(), "Consolidates current events of a tank to reservoir", (properties, args) -> tankSlack.consolidate(properties));
		add("add-function".toLowerCase(), "", java.util.Arrays.asList("name", "code"), java.util.Arrays.asList(), "Create a function associated to an input tank and output tank", (properties, args) -> nessieSlack.addFunction(properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : ""));
		add("pump".toLowerCase(), "", java.util.Arrays.asList("functionName", "input", "output"), java.util.Arrays.asList(), "Connect a source and destination tanks through a `function`", (properties, args) -> nessieSlack.pump(properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
		add("migrate".toLowerCase(), "", java.util.Arrays.asList("tank", "functions"), java.util.Arrays.asList(), "Reproduce events of a tank", (properties, args) -> nessieSlack.migrate(properties, args.length > 0 ? args[0] : "", args));
		add("reflow".toLowerCase(), "", java.util.Arrays.asList("tank"), java.util.Arrays.asList(), "Reproduce events of a tank", (properties, args) -> nessieSlack.reflow(properties, args.length > 0 ? args[0] : ""));
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