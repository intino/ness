package io.intino.ness.konos.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import io.intino.konos.slack.Bot.MessageProperties;
import io.intino.ness.Channel;
import io.intino.ness.DatalakeManager;
import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.bus.BusManager;
import io.intino.ness.konos.NessBox;

import java.util.List;
import java.util.Map;

import static io.intino.ness.konos.slack.Helper.*;

public class NessieSlack {

	private static final String OK = ":ok_hand:";
	private NessBox box;

	public NessieSlack(NessBox box) {
		this.box = box;
	}

	public void init(SlackSession session) {

	}

	public String manage(MessageProperties properties) {
		properties.context().command("manage");
		return "Now you are in management area";
	}

	public String users(MessageProperties properties) {
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

	public String channels(MessageProperties properties, String[] tags) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Channel> channel = ness.channelList();
		for (int i = 0; i < channel.size(); i++) {
			Channel topic = channel.get(i);
			if (tags.length == 0 || isTagged(tags, topic.tags()))
				builder.append(i + 1).append(") ").append(topic.qualifiedName());
			if (!topic.tags().isEmpty()) builder.append(" {").append(String.join(" ", topic.tags())).append("}");
			builder.append("\n");
		}
		String value = builder.toString();
		return value.isEmpty() ? "No channel" : value;
	}

	public String functions(MessageProperties properties) {
		Ness ness = ness(box);
		StringBuilder builder = new StringBuilder();
		List<Function> functions = ness.functionList();
		for (int i = 0; i < functions.size(); i++)
			builder.append(i).append(") ").append(functions.get(i).name()).append(". Being used on:...\n");
		String value = builder.toString();
		return value.isEmpty() ? "No functions" : value;
	}

	public String channel(MessageProperties properties, String name) {
		Channel channel = findChannel(box, name);
		if (channel == null) return "channel not found";
		properties.context().command("channel");
		properties.context().objects(name);
		return "Selected " + channel.qualifiedName();
	}

	public String addFunction(MessageProperties properties, String name, String code) {
		Ness ness = ness(box);
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name().equals(name));
		if (!functions.isEmpty()) return "function name is already defined";
		if (!datalake().isCorrect(code)) return "Code has errors or does not complies with NessFunction interface";
		Function function = ness.create("functions", name).function(sourceCode);
		function.save();
		return OK;
	}

	public String pump(MessageProperties properties, String functionName, String input, String output) {
		Ness ness = ness(box);
		List<Function> functions = ness.functionList(f -> f.name().equals(functionName));
		if (functions.isEmpty()) return "Function not found";
		Function function = functions.get(0);
		datalake().pump(function, input, output);
		return OK;
	}

	private DatalakeManager datalake() {
		return box.get(DatalakeManager.class);
	}

	public String reflow(MessageProperties properties, String channelName) {
		Channel channel = findChannel(box, channelName);
		if (channel == null) return "Channel not found";
		DatalakeManager manager = datalake();
		manager.reflow(channel);
		return OK;
	}

	private String nextVersionOf(Channel channel) {
		return channel.name().replace("." + channel.version(), "." + channel.version() + 1);
	}

	public String migrate(MessageProperties properties, String channelName, String[] args) {
		Channel channel = findChannel(box, channelName);
		String newChannelName = nextVersionOf(channel);
		Channel newChannel = ness(box).create("channels", newChannelName).channel(newChannelName);
		datalake().migrate(channel, newChannel);
		return OK;
	}
}