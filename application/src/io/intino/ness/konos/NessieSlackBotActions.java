package io.intino.ness.konos;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.Topic;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class NessieSlackBotActions {

	private static Map<String, String> selectedTopicByUser = new HashMap<>();

	static String help(NessBox box, MessageProperties properties, Map<String, Bot.CommandInfo> info) {
		return "";
	}

	public static void init(NessBox box, SlackSession session) {

	}

	static String topics(NessBox box, MessageProperties properties, String[] tags) {
		Ness ness = box.graph().wrapper(Ness.class);
		StringBuilder builder = new StringBuilder();
		List<Topic> topics = ness.topicList();
		for (int i = 0; i < topics.size(); i++) {
			Topic topic = topics.get(i);
			if (tags.length == 0 || isTagged(tags, topic.tags()))
				builder.append(i).append(") ").append(topic.name$());
			if (!topic.tags().isEmpty()) builder.append(" {").append(String.join(" ", topic.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "No topics" : value;
	}

	private static boolean isTagged(String[] tags, List<String> topicTags) {
		return Arrays.stream(tags).anyMatch(topicTags::contains);
	}

	static String topic(NessBox box, MessageProperties properties, String name) {
		Ness ness = box.graph().wrapper(Ness.class);
		Topic topic = findTopic(name, ness);
		if (topic == null) return "topic not found";
		selectedTopicByUser.put(properties.username(), topic.name$());
		return "Selected " + name;
	}

	static String functions(NessBox box, MessageProperties properties) {
		Ness ness = box.graph().wrapper(Ness.class);
		StringBuilder builder = new StringBuilder();
		List<Function> topics = ness.functionList();
		for (int i = 0; i < topics.size(); i++)
			builder.append(i).append(") ").append(topics.get(i).name()).append(". Being used on:...\n");
		String value = builder.toString();
		return value.isEmpty() ? "No functions" : value;
	}

	static String addFunction(NessBox box, MessageProperties properties, String name, String code) {
		Ness ness = box.graph().wrapper(Ness.class);
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name().equals(name));
		if (!functions.isEmpty()) return "function name is already defined";
		ness.create(name).function(sourceCode);
		return "";
	}

	static String tag(NessBox box, MessageProperties properties, String[] tags) {
		Ness ness = box.graph().wrapper(Ness.class);
		String topicName = selectedTopicByUser.get(properties.username());
		if (topicName == null) return "Please select a topic";
		Topic topic = findTopic(topicName, ness);
		topic.tags().clear();
		Collections.addAll(topic.tags(), tags);
		return ":ok_hand:";
	}

	static String pump(NessBox box, MessageProperties properties, String functionName, String input, String output) {
		Ness ness = box.graph().wrapper(Ness.class);
		return ":ok_hand:";
	}

	static String mount(NessBox box, MessageProperties properties, String since) {
		Ness ness = box.graph().wrapper(Ness.class);
		return ":ok_hand:";
	}

	static String consolidate(NessBox box, MessageProperties properties, String topic) {
		Ness ness = box.graph().wrapper(Ness.class);
		return ":ok_hand:";
	}

	private static Topic findTopic(String name, Ness ness) {
		List<Topic> topics = ness.topicList(t -> t.name$().equalsIgnoreCase(name));
		return topics.isEmpty() ? null : topics.get(0);
	}

	private static String downloadFile(String code) {
		try {
			Scanner scanner = new Scanner(new URL(code).openStream(), "UTF-8").useDelimiter("\\A");
			String sourceCode = scanner.hasNext() ? scanner.next() : "";
			scanner.close();
			return sourceCode;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}