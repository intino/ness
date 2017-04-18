package io.intino.ness.konos;

import io.intino.konos.slack.Bot;
import com.ullink.slack.simpleslackapi.SlackAttachment;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NessieSlackBot extends Bot {
	private static Logger LOG = Logger.getGlobal();


	public NessieSlackBot(NessBox box) {
		super(box.configuration().nessieConfiguration().token);
		add("help", java.util.Collections.emptyList(), java.util.Collections.emptyList(), "Show this help", (properties, args) -> {
            final String help = NessieSlackBotActions.help(box, properties, this.getCommandsInfo());
            return help == null || help.isEmpty() ? help() : help;
        });
		add("users".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Request all users registered pipe ness", (properties, args) -> NessieSlackBotActions.users(box, properties));
		add("add-user".toLowerCase(), java.util.Arrays.asList("name", "groups"), java.util.Arrays.asList(), "Request all topics nessy is subscribed filtered by tags", (properties, args) -> NessieSlackBotActions.addUser(box, properties, args.length > 0 ? args[0] : "", args));
		add("remove-user".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Request all topics nessy is subscribed filtered by tags", (properties, args) -> NessieSlackBotActions.removeUser(box, properties, args.length > 0 ? args[0] : ""));
		add("topics".toLowerCase(), java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Request all topics nessy is subscribed filtered by tags", (properties, args) -> NessieSlackBotActions.topics(box, properties, args));
		add("topic".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList("tag", "rename", "consolidate"), "Select a topic to operate map", (properties, args) -> NessieSlackBotActions.topic(box, properties, args.length > 0 ? args[0] : ""));
		add("tag".toLowerCase(), java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Tags thes topic map `tags`", (properties, args) -> NessieSlackBotActions.tag(box, properties, args));
		add("rename".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Changes name of this topic to new one", (properties, args) -> NessieSlackBotActions.rename(box, properties, args.length > 0 ? args[0] : ""));
		add("consolidate".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Consolidates current events of a topic to reservoir", (properties, args) -> NessieSlackBotActions.consolidate(box, properties));
		add("remove-topic".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Select a topic to operate map", (properties, args) -> NessieSlackBotActions.removeTopic(box, properties, args.length > 0 ? args[0] : ""));
		add("clear".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Clear all topics pipe ness", (properties, args) -> NessieSlackBotActions.clear(box, properties, args.length > 0 ? args[0] : ""));
		add("functions".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Show all functions registered", (properties, args) -> NessieSlackBotActions.functions(box, properties));
		add("add-function".toLowerCase(), java.util.Arrays.asList("name", "code"), java.util.Arrays.asList(), "Create a function associated to an input topic and output topic", (properties, args) -> NessieSlackBotActions.addFunction(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : ""));
		add("pump".toLowerCase(), java.util.Arrays.asList("functionName", "input", "output"), java.util.Arrays.asList(), "Connect a source and destination topics by a `function`", (properties, args) -> NessieSlackBotActions.pump(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
		add("mount".toLowerCase(), java.util.Arrays.asList("since"), java.util.Arrays.asList(), "Reproduce events of a topic", (properties, args) -> NessieSlackBotActions.mount(box, properties, args.length > 0 ? args[0] : ""));
		try {
			execute();
			NessieSlackBotActions.init(box, session());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}