package io.intino.ness.terminal.builder.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtils {

	public static String getMessage(Throwable e) {
		StringWriter str = new StringWriter();
		try(PrintWriter writer = new PrintWriter(str)) {
			e.printStackTrace(writer);
			return str.toString();
		}
	}
}
