package io.intino.ness.konos;

import io.intino.konos.slack.Bot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NessySlackBot extends Bot {
	private static Logger LOG = Logger.getGlobal();


	public NessySlackBot(ApplicationBox box) {
		super(box.configuration().nessyConfiguration().token);
		add("help", java.util.Collections.emptyList(), java.util.Collections.emptyList(), "Show this help", (properties, args) -> {
            final String help = NessySlackBotActions.help(box, properties, this.getCommandsInfo());
            return help == null || help.isEmpty() ? help() : help;
        });
		add("topics".toLowerCase(), java.util.Arrays.asList(), java.util.Arrays.asList(), "Request all topics nessy is subscribed", (properties, args) -> NessySlackBotActions.topics(box, properties));
		add("topic".toLowerCase(), java.util.Arrays.asList("name"), java.util.Arrays.asList(), "Select a topic to operate with", (properties, args) -> NessySlackBotActions.topic(box, properties, args.length > 0 ? args[0] : ""));
		add("create".toLowerCase(), java.util.Arrays.asList("name", "code"), java.util.Arrays.asList(), "Create a function associated to an input topic and output topic", (properties, args) -> NessySlackBotActions.create(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : ""));
		add("pump".toLowerCase(), java.util.Arrays.asList("functionName", "input", "output"), java.util.Arrays.asList(), "Connect a source and destination topics by a `function`", (properties, args) -> NessySlackBotActions.pump(box, properties, args.length > 0 ? args[0] : "", args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
		add("mount".toLowerCase(), java.util.Arrays.asList("since"), java.util.Arrays.asList(), "Reproduce events of a topic", (properties, args) -> NessySlackBotActions.mount(box, properties, args.length > 0 ? args[0] : ""));
		add("consolidate".toLowerCase(), java.util.Arrays.asList("topic"), java.util.Arrays.asList(), "Consolidates current events of a topic to reservoir", (properties, args) -> NessySlackBotActions.consolidate(box, properties, args.length > 0 ? args[0] : ""));
		try {
			execute();
			NessySlackBotActions.init(box, session());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}