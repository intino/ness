package io.intino.master.data.validation.report;

import io.intino.master.data.validation.Issue;
import io.intino.master.data.validation.TripleSource;
import io.intino.master.data.validation.report.IssueReport.IssuesCount;
import io.intino.master.data.validation.validators.DuplicatedTripleRecordValidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class HtmlIssueReportDocumentBuilder {

	private static final String BLANK_LINE = "<p>&nbsp;</p>";

	private final IssueReport issueReport;

	public HtmlIssueReportDocumentBuilder(IssueReport issueReport) {
		this.issueReport = issueReport;
	}

	public void build(File file) {
		file.getParentFile().mkdirs();

		HtmlBuilder builder = new HtmlBuilder();
		builder.addHtmlTag(false);

		HtmlTemplate template = HtmlTemplate.get("issues-report.html");
		template.set("count", String.valueOf(issueReport.count()));
		template.set("error-count", String.valueOf(issueReport.errorCount()));
		template.set("warnings-count", String.valueOf(issueReport.warningCount()));

		template.set("sources-count", String.valueOf(issueReport.getAll().size()));

		template.set("issues", renderIssues());
		template.set("issues-levels-chart-data", renderIssuesLevelsChartData());
		template.set("issues-types-chart-data", renderIssuesTypesChartData());
		template.set("content", renderContent());

		builder.append(template.html());

		try {
			Files.writeString(file.toPath(), builder.build());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String renderContent1() {
		StringBuilder sb = new StringBuilder();
		for(var entry : sortByNumErrors(new HashSet<>(issueReport.getAll().entrySet()))) {
			sb.append("<h4 id=\"").append(entry.getKey().hashCode()).append("\">").append(entry.getKey()).append(" (").append(entry.getValue().size()).append("):</h4>");
			sb.append("<ul class=\"list-group\">");
			entry.getValue().stream().filter(e -> e.level() == Issue.Level.Error).sorted(sortByLine()).map(this::render).forEach(sb::append);
			entry.getValue().stream().filter(e -> e.level() == Issue.Level.Warning).sorted(sortByLine()).map(this::render).forEach(sb::append);
			sb.append("</ul>").append(BLANK_LINE);
		}
		return sb.toString();
	}

	private String renderContent() {
		StringBuilder sb = new StringBuilder();

		sb.append("<div class=\"accordion\" id=\"accordionContent\">");

		int index = 0;

		for(var entry : sortByNumErrors(new HashSet<>(issueReport.getAll().entrySet()))) {
			renderCard(sb, entry, index++);
		}

		sb.append("</div>");

		return sb.toString();
	}

	private void renderCard(StringBuilder sb, Map.Entry<String, List<Issue>> entry, int index) {
		int errors = numErrors(entry.getValue());
		int warnings = entry.getValue().size() - errors;

		sb.append("<div class=\"card\">");
		// Head: source + badges
		sb.append("<div class=\"card-header d-flex\" id=\"heading").append(index).append("\">");
		sb.append("<h5 class=\"mb-0\">");
		sb.append(String.format("<div class=\"btn\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapse%d\" aria-expanded=\"true\" aria-controls=\"collapse%d\">", index, index));
		sb.append("<div>").append(entry.getKey()).append("</div>");
		sb.append("</div></h5>");
		sb.append(badgeGroup(warnings, errors));
		sb.append("</div>");
		// Body: list of issues
		sb.append(String.format("<div id=\"collapse%d\" class=\"collapse\" aria-labelledby=\"heading%d\" data-parent=\"#accordionContent\">", index, index));
		sb.append("<div class=\"card-body\">");
		sb.append("<ul class=\"list-group\">");
		entry.getValue().stream().filter(e -> e.level() == Issue.Level.Error).sorted(sortByLine()).map(this::render).forEach(sb::append);
		entry.getValue().stream().filter(e -> e.level() == Issue.Level.Warning).sorted(sortByLine()).map(this::render).forEach(sb::append);
		sb.append("</ul>");
		sb.append("</div></div></div>");
	}

	private String badgeGroup(int warnings, int errors) {
		return "<div class=\"d-flex align-items-right ml-auto align-items-center ml-auto\">\n" +
				"<span class=\"badge badge-danger badge-pill mr-1\">" + errors + "</span>\n" +
				"<span class=\"badge badge-warning badge-pill mr-1\">" + warnings + "</span>\n" +
				"<span class=\"badge badge-primary badge-pill mr-1\">" + (errors + warnings) + "</span>\n" +
				"</div>";
	}

	private Comparator<? super Issue> sortByLine() {
		return Comparator.comparing(issue -> !(issue.source() instanceof TripleSource.FileTripleSource) ? Integer.MAX_VALUE : ((TripleSource.FileTripleSource) issue.source()).line());
	}

	private String render(Issue issue) {
		if(issue.source() instanceof DuplicatedTripleRecordValidator.CombinedTripleSource) return renderIssueCombinedSource(issue);
		String level = issue.level() == Issue.Level.Error ? "danger" : "warning";
		return "<div class=\"list-group-item list-group-item-" + level + " mb-1\">"
				+ "<div><i class=\"fa-solid fa-skating fa-fw\" style=\"background:DodgerBlue\"></i>" + issue.levelMsg() + "</div>"
				+ (issue.source() == null ? "" : ("<small>At " + issue.source().get() + "</small>"))
				+ "</div>";
	}

	private String renderIssueCombinedSource(Issue issue) {
		String level = issue.level() == Issue.Level.Error ? "danger" : "warning";

		StringBuilder sb = new StringBuilder("<div class=\"list-group-item list-group-item-" + level + " mb-1\">");
		sb.append("<p><b>[").append(issue.level().name()).append("]</b> ").append(issue.message()).append("</p>");

		for(String name : ((DuplicatedTripleRecordValidator.CombinedTripleSource)issue.source()).names()) {
			sb.append("<p><small>At ").append(name).append("</small></p>");
		}

		return sb.append("</div>").toString();
	}

	private String renderIssues() {
		StringBuilder sb = new StringBuilder();
		float total = issueReport.count();
		for(Map.Entry<String, IssuesCount> entry : issueReport.issueTypes().entrySet()) {
			IssuesCount count = entry.getValue();
			String percentage = String.format("%.02f", count.total() / total * 100);
			sb.append(listItemBadge(entry.getKey() + " <b>(" + percentage + "%)</b>", count.warnings(), count.errors()));
		}
		return sb.toString();
	}

	private String renderIssuesLevelsChartData() {
		StringBuilder sb = new StringBuilder();
		int errors = issueReport.errorCount();
		int warnings = issueReport.warningCount();
		float total = errors + warnings;

		sb.append("{ name: '").append("Errors").append("'")
				.append(", y: ").append(String.format("%.02f", errors / total * 100).replace(",", "."))
				.append("},");

		sb.append("{ name: '").append("Warnings").append("'")
				.append(", y: ").append(String.format("%.02f", warnings / total * 100).replace(",", "."))
				.append("}");

		return sb.toString();
	}

	private String renderIssuesTypesChartData() {
		StringBuilder sb = new StringBuilder();
		float total = issueReport.count();
		boolean first = true;
		for(Map.Entry<String, IssuesCount> entry : issueReport.issueTypes().entrySet()) {
			if(!first) sb.append(", ");
			first = false;
			IssuesCount count = entry.getValue();
			String percentage = String.format("%.02f", count.total() / total * 100).replace(",", ".");
			sb.append("{ name: '").append(entry.getKey()).append("'").append(", y: ").append(percentage).append("}");
		}
		return sb.toString();
	}

	private List<? extends Map.Entry<String, List<Issue>>> sortByNumErrors(Set<Map.Entry<String, List<Issue>>> entrySet) {
		return entrySet.stream().sorted((e1, e2) -> -Integer.compare(numErrors(e1.getValue()), numErrors(e2.getValue()))).collect(toList());
	}

	private int numErrors(List<Issue> issueList) {
		return (int) issueList.stream().filter(issue -> issue.level().equals(Issue.Level.Error)).count();
	}

	private String listItemBadge(String text, int warnings, int errors) {
		return "<li class=\"list-group-item d-flex\">"
				+ "<div>"
				+ text
				+ "</div>\n"
				+ "<div class=\"d-flex align-items-right ml-auto align-items-center ml-auto\">"
				+ "<span class=\"badge badge-danger badge-pill mr-1\">" + errors + "</span>"
				+ "<span class=\"badge badge-warning badge-pill mr-1\">" + warnings + "</span>"
				+ "<span class=\"badge badge-primary badge-pill mr-1\">" + (warnings + errors) + "</span>"
				+ "</div>\n"
				+ "<span class=\"border-bottom-1\"></span>"
				+ "</li>";
	}

	public static class HtmlBuilder {

		private final StringBuilder html;
		private boolean addHtmlTag = true;

		public HtmlBuilder() {
			this.html = new StringBuilder(8192);
		}

		public HtmlBuilder addHtmlTag(boolean addHtmlTag) {
			this.addHtmlTag = addHtmlTag;
			return this;
		}

		public HtmlBuilder append(HtmlTemplate template) {
			this.html.append(template.html());
			return this;
		}

		public HtmlBuilder append(String str) {
			this.html.append(str);
			return this;
		}

		public String build() {
			return addHtmlTag ? "<html>" + html + "</html>" : html.toString();
		}

		@Override
		public String toString() {
			return build();
		}
	}

	public static class HtmlTemplate {

		public static HtmlTemplate get(String name) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(HtmlTemplate.class.getResourceAsStream("/" + name)))) {
				return new HtmlTemplate(reader.lines().collect(Collectors.joining("\n")));
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to open " + name, e);
			}
		}

		private final StringBuilder html;

		public HtmlTemplate(String html) {
			this.html = new StringBuilder(html);
		}

		public HtmlTemplate set(String variable, String value) {
			variable = String.format("'$%s'", variable);
			int index;
			while((index = html.indexOf(variable)) >= 0) {
				html.replace(index, index + variable.length(), value);
			}
			return this;
		}

		public String html() {
			return html.toString();
		}

		@Override
		public String toString() {
			return html();
		}
	}
}
