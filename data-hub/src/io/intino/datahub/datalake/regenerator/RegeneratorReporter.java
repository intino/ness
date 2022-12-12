package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RegeneratorReporter {
	private static final String htmlTablePrefix = "<html>" +
			"<head>\n" +
			"<style>\n" +
			"html, body {\n" +
			"\theight: 100%;\n" +
			"}\n" +
			"\n" +
			"table {\n" +
			"    width: 100%;\n" +
			"}\n" +
			"table, th, td {\n" +
			"  border: 1px solid black;\n" +
			"}\n" +
			"\n" +
			"table tr td {\n" +
			"    width: 50%;\n" +
			"    overflow-y: scroll;\n" +
			"}\n" +
			"\n" +
			"table tr td div {\n" +
			"\twidth:1px;\n" +
			"}\n" +
			"\n" +
			"td {\n" +
			"  vertical-align: top;\n" +
			"}\n" +
			"</style>\n" +
			"</head>" +
			"<body>" +
			"<table style=\"width:100%\">\n" +
			"  <col width=\"50%\">\n" +
			"  <col width=\"50%\">" +
			"<tr>\n" +
			"\t<th>Before</th>\n" +
			"\t<th>After</th>\n" +
			"</tr>\n";
	private static final String htmlTableSuffix = "</table>\n" +
			"</body></html>";
	private final Path destination;
	private BufferedWriter writer;


	public RegeneratorReporter(File destination) {
		this.destination = destination.toPath();
		destination.getParentFile().mkdirs();
		try {
			Files.write(this.destination, htmlTablePrefix.getBytes());
			writer = new BufferedWriter(new FileWriter(destination));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void addItem(String before, String after) {
		if (before.equals(after)) return;
		StringBuilder builder = new StringBuilder();
		builder.append("<tr>\n").
				append("\t<td>\n").append("<pre>").append(before).append("</pre>\n</td>\n").append("<td>\n").append(after == null ? "REMOVE" : "<pre>" + after + "<pre>").append("\n</td>\n").
				append("</tr>");
		try {
			writer.write(builder.toString());
		} catch (IOException e) {
			Logger.error(e);
		}
	}


	public void commit() {
		try {
			writer.write(htmlTableSuffix);
			writer.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
