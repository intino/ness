package io.intino.ness.datahubterminalplugin.master;

import io.intino.datahub.model.EntityData;
import io.intino.datahub.model.Expression;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Character.isWhitespace;

public class ExpressionHelper {

	public static final String DEFAULT_ITR_INDENTATION = "        ";

	public static Frame exprFrameOf(Expression expr, String ontologyPackage) {
		FrameBuilder builder = new FrameBuilder("expression");

		builder.add("modifier", expr.isPrivate() ? "private" : "public");
		builder.add("name", expr.name$().trim());
		builder.add("returnType", returnTypeOf(expr, ontologyPackage));
		builder.add("expr", expressionOf(expr));

		List<Frame> parameters = parametersOf(expr, ontologyPackage);
		if(!parameters.isEmpty()) builder.add("parameter", parameters.toArray(Frame[]::new));

		return builder.toFrame();
	}

	private static String expressionOf(Expression expr) {
		return decorateExpr(expr, rawExpressionOf(expr));
	}

	private static String decorateExpr(Expression expr, String exprStr) {
		exprStr = exprStr.replace("System.out.", "java.lang.System.out.").trim();
		if(StringUtils.countMatches(exprStr, "\n") == 0) {
			if(!expr.isRoutine() && !exprStr.startsWith("return")) exprStr = "return " + exprStr;
			if(!exprStr.endsWith(";")) exprStr += ";";
			return exprStr;
		}
		String[] lines = exprStr.split("\n", -1);
		String indentation = indentationOf(lines);
		return Arrays.stream(lines).map(line -> removeFirstIndentation(line, indentation)).collect(Collectors.joining("\n"));
	}

	private static String removeFirstIndentation(String line, String indentation) {
		return line.startsWith(indentation) ? line.substring(indentation.length()) : line;
	}

	private static String indentationOf(String[] lines) {
		return indentationOf(Arrays.stream(lines).filter(l -> l.endsWith(";") && isWhitespace(l.charAt(0))).findFirst().orElse(""));
	}

	private static String indentationOf(String line) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; isWhitespace(line.charAt(i)); i++) {
			sb.append(line.charAt(i));
		}
		return sb.toString();
	}

	private static String rawExpressionOf(Expression expr) {
		try {
			List<?> expression = expr.core$().variables().get("expression");
			return (String) expression.get(0);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot read expression of " + expr.name$());
		}
	}

	private static List<Frame> parametersOf(Expression expr, String ontologyPackage) {
		if(expr.isGetter()) return Collections.emptyList();
		return expr.core$().componentList().stream()
				.filter(c -> c.is(Expression.Function.Parameter.class) || c.is(Expression.Routine.Parameter.class))
				.map(c -> frameOfParameter(c.as(EntityData.class), ontologyPackage))
				.collect(Collectors.toList());
	}

	private static Frame frameOfParameter(EntityData data, String ontologyPackage) {
		FrameBuilder builder = new FrameBuilder("parameter");
		builder.add("type", type(new ConceptAttribute(data, null), ontologyPackage));
		builder.add("name", data.name$());
		return builder.toFrame();
	}

	private static String returnTypeOf(Expression expr, String ontologyPackage) {
		if(expr.isFunction()) return type(new ConceptAttribute(expr.asFunction().returnType(), expr.core$()), ontologyPackage);
		if(expr.isDoubleGetter()) return "Double";
		if(expr.isIntegerGetter()) return "Integer";
		if(expr.isLongGetter()) return "Long";
		if(expr.isBooleanGetter()) return "Boolean";
		if(expr.isStringGetter()) return "String";
		if(expr.isDateGetter()) return "LocalDate";
		if(expr.isDateTimeGetter()) return "LocalDateTime";
		if(expr.isInstantGetter()) return "Instant";
		return "void";
	}

	private static String type(ConceptAttribute attr, String ontologyPackage) {
		String type = attr.type();

		if(attr.isEntity()) type = ontologyPackage + ".entities." + type;
		else if(attr.isStruct()) type = ontologyPackage + ".structs." + type;

		if(attr.isList()) return "List<" + type + ">";
		if(attr.isSet()) return "Set<" + type + ">";

		return type;
	}
}
