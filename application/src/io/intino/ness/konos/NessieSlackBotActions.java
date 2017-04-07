package io.intino.ness.konos;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.Topic;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.NessFunction;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ullink.slack.simpleslackapi.SlackAction.TYPE_BUTTON;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;

final class NessieSlackBotActions {

	private static Map<String, String> selectedTopicByUser = new HashMap<>();

	static String help(NessBox box, MessageProperties properties, Map<String, Bot.CommandInfo> info) {
		String help = "";
		if (selectedTopicByUser.get(properties.username()) == null) {
			for (String command : info.keySet())
				if (!isComponent(info, command)) help += formatCommand(info, command);
		} else for (String command : info.get("topic").components()) help += formatCommand(info, command);
		return help;
	}

	static void init(NessBox box, SlackSession session) {
	}

	static String users(NessBox box, MessageProperties properties) {
		Map<String, List<String>> users = box.get(BusManager.class).users();
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

	static String topics(NessBox box, MessageProperties properties, String[] tags) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Topic> topics = ness.topicList();
		for (int i = 0; i < topics.size(); i++) {
			Topic topic = topics.get(i);
			if (tags.length == 0 || isTagged(tags, topic.tags()))
				builder.append(i + 1).append(") ").append(topic.qualifiedName());
			if (!topic.tags().isEmpty()) builder.append(" {").append(String.join(" ", topic.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "No topics" : value;
	}

	static String removeTopic(NessBox box, MessageProperties properties, String name) {
		return box.get(BusManager.class).cleanTopic(name) ? ":ok:hand:" : "Topic not found";
	}

	static String addUser(NessBox box, MessageProperties properties, String name, String[] groups) {
		String password = box.get(BusManager.class).addUser(name, asList(copyOfRange(groups, 1, groups.length)));
		if (password == null) return "User already exists";
		return "User *" + name + "* added with password `" + password + "`";
	}

	static String removeUser(NessBox box, MessageProperties properties, String name) {
		return box.get(BusManager.class).removeUser(name) ? ":ok_hand:" : "User not found";
	}

	static String topic(NessBox box, MessageProperties properties, String name) {
		Ness ness = ness(box);
		Topic topic = findTopic(name, ness);
		if (topic == null) return "topic not found";
		selectedTopicByUser.put(properties.username(), topic.qualifiedName());
		return "Selected " + topic.qualifiedName();
	}

	static SlackAttachment clear(NessBox box, MessageProperties properties, String name) {
		SlackAttachment attachment = new SlackAttachment("Are you sure to clear this topic?", "", "", "");
		attachment.addAction("answer", "yes", "Yes", TYPE_BUTTON);
		attachment.addAction("answer", "no", "No", TYPE_BUTTON);
		return attachment;
	}

	static String rename(NessBox box, MessageProperties properties, String name) {
		String topicName = selectedTopicByUser.get(properties.username());
		if (topicName == null) return "Please select a topic";
		return box.get(BusManager.class).renameTopic(topicName, name) ? ":ok_hand:" : "Impossible to rename topic";
	}

	static String functions(NessBox box, MessageProperties properties) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Function> topics = ness.functionList();
		for (int i = 0; i < topics.size(); i++)
			builder.append(i).append(") ").append(topics.get(i).name()).append(". Being used on:...\n");
		String value = builder.toString();
		return value.isEmpty() ? "No functions" : value;
	}

	static String addFunction(NessBox box, MessageProperties properties, String name, String code) {
		Ness ness = ness(box);
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name().equals(name));
		if (!functions.isEmpty()) return "function name is already defined";
		ness.create(name).function(sourceCode);
		return "";
	}

	static String tag(NessBox box, MessageProperties properties, String[] tags) {
		Ness ness = ness(box);
		String topicName = selectedTopicByUser.get(properties.username());
		if (topicName == null) return "Please select a topic";
		Topic topic = findTopic(topicName, ness);
		topic.tags().clear();
		Collections.addAll(topic.tags(), tags);
		return ":ok_hand:";
	}

	static String pump(NessBox box, MessageProperties properties, String functionName, String input, String output) {
//		NessFunctionContainer container = new NessFunctionContainer(box.get(FileDataLake.class));
//		container.pump(input).with(searchFunction(functionName)).into(output).start();
		return ":ok_hand:";
	}

	private static Class<? extends NessFunction> searchFunction(String functionName) {
		return null;
	}

	static String mount(NessBox box, MessageProperties properties, String since) {
		Ness ness = ness(box);
		return ":ok_hand:";
	}

	static String consolidate(NessBox box, MessageProperties properties) {
		Ness ness = ness(box);
		return ":ok_hand:";
	}

	private static Ness ness(NessBox box) {
		return box.graph().wrapper(Ness.class);
	}

	private static boolean isTagged(String[] tags, List<String> topicTags) {
		return Arrays.stream(tags).anyMatch(topicTags::contains);
	}

	private static Topic findTopic(String name, Ness ness) {
		List<Topic> topics = ness.topicList(t -> t.qualifiedName().equalsIgnoreCase(name));
		return topics.isEmpty() ? findByPosition(name, ness) : topics.get(0);
	}


	private static Topic findByPosition(String name, Ness ness) {
		final List<Topic> topics = sortedTopics(ness).collect(Collectors.toList());
		try {
			final int position = Integer.parseInt(name);
			return position <= topics.size() ? topics.get(position - 1) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Stream<Topic> sortedTopics(Ness ness) {
		return ness.topicList().stream().sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.qualifiedName(), s2.qualifiedName()));
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

	private static boolean isComponent(Map<String, Bot.CommandInfo> info, String command) {
		for (Bot.CommandInfo commandInfo : info.values()) if (commandInfo.components().contains(command)) return true;
		return false;
	}

	private static String formatCommand(Map<String, Bot.CommandInfo> info, String command) {
		return "`" + command + helpParameters(info.get(command).parameters()) + "` " + info.get(command).description() + "\n";
	}

	private static String helpParameters(List<String> parameters) {
		return parameters.isEmpty() ? "" : " <" + String.join("> <", parameters) + ">";
	}

}