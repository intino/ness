package io.intino.master.data.validation.report;

import io.intino.alexandria.logger.Logger;
import io.intino.master.data.validation.Issue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.intino.master.data.validation.report.IssueReport.OTHER;
import static java.util.stream.Collectors.toList;

public class TxtIssueReportDocumentBuilder {

	private final IssueReport issueReport;

	public TxtIssueReportDocumentBuilder(IssueReport issueReport) {
		this.issueReport = issueReport;
	}

	public void build(File file) {
		file.getParentFile().mkdirs();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			title(writer);

			for (Map.Entry<String, List<Issue>> entry : sortByNumErrors(issueReport.getAll().entrySet())) {

				String path = entry.getKey();
				List<Issue> issues = entry.getValue();
				if (issues.isEmpty()) continue;

				issues.sort(Comparator.naturalOrder());

				writer.newLine();
				writer.newLine();
				println(writer, path.equals(OTHER)
						? "==== General issues (" + issues.size() + ") ====\n"
						: "==== '" + path + "' (Issues: " + issues.size() + ") ====\n");

				printAll(writer, issues.stream().filter(i -> i.level() == Issue.Level.Error));
				printAll(writer, issues.stream().filter(i -> i.level() == Issue.Level.Warning));
			}

		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void title(BufferedWriter writer) {
		println(writer, "MASTER ISSUES REPORT\n");
		println(writer, "Date: " + LocalDateTime.now());
		println(writer, "Errors: " + issueReport.errorCount());
		println(writer, "Warnings: " + issueReport.warningCount());
		println(writer, "Files with problems: " + issueReport.getAll().size());
	}

	private Iterable<? extends Map.Entry<String, List<Issue>>> sortByNumErrors(Set<Map.Entry<String, List<Issue>>> entrySet) {
		return entrySet.stream().sorted((e1, e2) -> -Integer.compare(numErrors(e1.getValue()), numErrors(e2.getValue()))).collect(toList());
	}

	private int numErrors(List<Issue> issueList) {
		return (int) issueList.stream().filter(issue -> issue.level().equals(Issue.Level.Error)).count();
	}

	private void printAll(BufferedWriter writer, Stream<Issue> issues) {
		issues.forEach(issue -> println(writer, issue.toString() + "\n"));
	}

	private void println(BufferedWriter writer, String msg) {
		try {
			writer.write(msg);
			writer.newLine();
		} catch (Exception ignored) {
		}
	}
}
