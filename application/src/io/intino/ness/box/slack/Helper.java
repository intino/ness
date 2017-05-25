package io.intino.ness.box.slack;

import io.intino.ness.Tank;
import io.intino.ness.NessGraph;
import io.intino.ness.box.NessBox;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {

	public static Tank findTank(NessBox box, String name) {
		List<Tank> topics = box.ness().tankList(t -> t.qualifiedName().equalsIgnoreCase(name)).collect(Collectors.toList());
		return topics.isEmpty() ? findByPosition(box, name) : topics.get(0);
	}

	public static boolean isTagged(String[] tags, List<String> topicTags) {
		return Arrays.stream(tags).anyMatch(topicTags::contains);
	}

	public static boolean isTagged(List<String> tags, List<String> topicTags) {
		return tags.stream().anyMatch(topicTags::contains);
	}

	private static Tank findByPosition(NessBox box, String name) {
		final List<Tank> topics = sortedTanks(box.ness()).collect(Collectors.toList());
		try {
			final int position = Integer.parseInt(name);
			return position <= topics.size() ? topics.get(position - 1) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Stream<Tank> sortedTanks(NessGraph ness) {
		return ness.tankList().stream().sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.qualifiedName(), s2.qualifiedName()));
	}

	public static String downloadFile(String code) {
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
