package io.intino.ness.konos;

import io.intino.konos.slack.Bot;
import io.intino.konos.slack.Bot.MessageProperties;

import java.util.Map;

public class NessySlackBotActions {

    static String help(ApplicationBox box, MessageProperties properties, Map<String, Bot.CommandInfo> info) {
        return "";
    }

    public static void init(ApplicationBox box, com.ullink.slack.simpleslackapi.SlackSession session) {

    }

	static String topics(ApplicationBox box, MessageProperties properties) {
		return "";
	}

	static String topic(ApplicationBox box, MessageProperties properties, String name) {
		return "";
	}

	static String create(ApplicationBox box, MessageProperties properties, String name, String code) {
		return "";
	}

	static String pump(ApplicationBox box, MessageProperties properties, String functionName, String input, String output) {
		return "";
	}

	static String mount(ApplicationBox box, MessageProperties properties, String since) {
		return "";
	}

	static String consolidate(ApplicationBox box, MessageProperties properties, String topic) {
		return "";
	}
}