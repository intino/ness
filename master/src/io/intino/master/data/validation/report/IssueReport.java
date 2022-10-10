package io.intino.master.data.validation.report;

import io.intino.master.data.validation.Issue;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class IssueReport {

	public static final String OTHER = "Other";

	private final Map<String, List<Issue>> issues;

	public IssueReport() {
		this.issues = new HashMap<>();
	}

	public IssueReport put(Stream<Issue> issues) {
		if (issues == null) return this;
		issues.forEach(this::put);
		return this;
	}

	private void put(Issue issue) {
		if (issue == null) return;
		String key = issue.source() == null ? OTHER : issue.source().name();
		if(key == null) key = OTHER;
		List<Issue> issuesOfThatFile = issues.computeIfAbsent(key, k -> new ArrayList<>());
		issuesOfThatFile.add(issue);
	}

	public Map<String, List<Issue>> getAll() {
		return Collections.unmodifiableMap(issues);
	}

	public int errorCount() {
		return (int) filter(Issue.Level.Error).count();
	}

	public int warningCount() {
		return (int) filter(Issue.Level.Warning).count();
	}

	public List<Issue> errors() {
		return filter(Issue.Level.Error).collect(toList());
	}

	public List<Issue> warnings() {
		return filter(Issue.Level.Warning).collect(toList());
	}

	public int count() {
		return issues.values().stream().mapToInt(List::size).sum();
	}

	public Map<String, IssuesCount> issueTypes() {
		Map<String, IssuesCount> types = new HashMap<>();
		for(var issueList : issues.values()) issueList
				.forEach(i -> types.compute(i.type(), (k, v) -> {
					IssuesCount count = v == null ? new IssuesCount() : v;
					if(i.level() == Issue.Level.Error) count.errors++;
					else count.warnings++;
					return count;
				}));
		return types.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(
				Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new
		));
	}

	private Stream<Issue> filter(Issue.Level level) {
		return issues.values().stream().flatMap(List::stream).filter(i -> i.level().equals(level));
	}

	public void save(File file) {
		new HtmlIssueReportDocumentBuilder(this).build(new File(file.getAbsolutePath().replace(".txt", ".html")));
		new TxtIssueReportDocumentBuilder(this).build(new File(file.getAbsolutePath().replace(".html", ".txt")));
	}

	public static class IssuesCount implements Comparable<IssuesCount> {

		private int warnings;
		private int errors;

		private IssuesCount warnings(int warnings) {
			this.warnings = warnings;
			return this;
		}

		private IssuesCount errors(int errors) {
			this.errors = errors;
			return this;
		}

		public int warnings() {
			return warnings;
		}

		public int errors() {
			return errors;
		}

		public int total() {
			return warnings + errors;
		}

		@Override
		public int compareTo(IssuesCount o) {
			int cmp = Integer.compare(o.errors, errors);
			return cmp == 0 ? Integer.compare(o.warnings, warnings) : cmp;
		}
	}
}
