package io.intino.ness.konos;

import io.intino.konos.slack.Bot;

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
		add("topics".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Request all topics nessy is subscribed", (properties, args) -> NessieSlackBotActions.topics(box, properties));
		add("topic".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Select a topic to operate with", (properties, args) -> NessieSlackBotActions.topic(box, properties, args.length > 0 ? args[0] : ""));
		add("functions".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Show all functions registered", (properties, args) -> NessieSlackBotActions.functions(box, properties));
		add("add-function".toLowerCase(), java.util.Arrays.asList("name", "code"), java.util.Arrays.asList(), "Create a function associated to an input topic and output topic", (properties, args) -> NessieSlackBotActions.addFunction(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : ""));
		add("tag".toLowerCase(), java.util.Arrays.asList("tags"), java.util.Arrays.asList(), "Tag a topic with `tags`", (properties, args) -> NessieSlackBotActions.tag(box, properties, args));
		add("pump".toLowerCase(), java.util.Arrays.asList("functionName", "input", "output"), java.util.Arrays.asList(), "Connect a source and destination topics by a `function`", (properties, args) -> NessieSlackBotActions.pump(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
		add("mount".toLowerCase(), java.util.Arrays.asList("since"), java.util.Arrays.asList(), "Reproduce events of a topic", (properties, args) -> NessieSlackBotActions.mount(box, properties, args.length > 0 ? args[0] : ""));
		add("consolidate".toLowerCase(), java.util.Arrays.asList("topic"), java.util.Arrays.asList(), "Consolidates current events of a topic to reservoir", (properties, args) -> NessieSlackBotActions.consolidate(box, properties, args.length > 0 ? args[0] : ""));
		try {
			execute();
			NessieSlackBotActions.init(box, session());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}