package io.intino.ness.konos.slack;

import io.intino.ness.Channel;
import io.intino.ness.Ness;
import io.intino.ness.konos.NessBox;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {

	static Ness ness(NessBox box) {
		return box.graph().wrapper(Ness.class);
	}

	static Channel findChannel(NessBox box, String name) {
		List<Channel> topics = ness(box).channelList(t -> t.qualifiedName().equalsIgnoreCase(name));
		return topics.isEmpty() ? findByPosition(box, name) : topics.get(0);
	}

	static boolean isTagged(String[] tags, List<String> topicTags) {
		return Arrays.stream(tags).anyMatch(topicTags::contains);
	}

	static Channel findByPosition(NessBox box, String name) {
		final List<Channel> topics = sortedTopics(ness(box)).collect(Collectors.toList());
		try {
			final int position = Integer.parseInt(name);
			return position <= topics.size() ? topics.get(position - 1) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	static Stream<Channel> sortedTopics(Ness ness) {
		return ness.channelList().stream().sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.qualifiedName(), s2.qualifiedName()));
	}

	static String downloadFile(String code) {
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
