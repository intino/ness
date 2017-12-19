package io.intino.ness.box.slack;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {


	public static Tank findTank(NessGraph graph, String name) {
		List<Tank> topics = graph.tankList(t -> t.qualifiedName().equalsIgnoreCase(name)).collect(Collectors.toList());
		return topics.isEmpty() ? findByPosition(graph, name) : topics.get(0);
	}

	public static Tank findTank(NessBox box, String name) {
		List<Tank> topics = box.graph().tankList(t -> t.qualifiedName().equalsIgnoreCase(name)).collect(Collectors.toList());
		return topics.isEmpty() ? findByPosition(box.graph(), name) : topics.get(0);
	}

	public static boolean isTagged(String[] tags, List<String> topicTags) {
		return Arrays.stream(tags).anyMatch(topicTags::contains);
	}

	public static boolean isTagged(List<String> tags, List<String> topicTags) {
		return tags.stream().anyMatch(topicTags::contains);
	}

	private static Tank findByPosition(NessGraph ness, String name) {
		final List<Tank> topics = sortedTanks(ness).collect(Collectors.toList());
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

	public static String downloadTextFile(String url, String slackToken) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("Authorization", "Bearer " + slackToken);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) try (InputStream stream = entity.getContent()) {
				StringBuilder builder = new StringBuilder();
				BufferedReader reader =
						new BufferedReader(new InputStreamReader(stream));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line).append("\n");
				}
				return builder.toString();
			}
		} catch (Exception ignored) {
		}
		return "";
	}
}
