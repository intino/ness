package io.intino.ness.datahubterminalplugin.master;

import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Expression;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.master.EntityFrameCreator.*;

public class ExpressionHelper {

	public static final String DEFAULT_ITR_INDENTATION = "        ";

	public static Frame exprFrameOf(Node node) {
		FrameBuilder builder = new FrameBuilder().add("expression");

		Expression expr = node.as(Expression.class);

		builder.add("modifier", expr.isPrivate() ? "private" : "public");
		builder.add("name", expr.name$());
		builder.add("returnType", returnTypeOf(expr));
		builder.add("expression", expressionOf(expr));

		List<Frame> parameters = parametersOf(expr);
		if(!parameters.isEmpty()) builder.add("parameter", parameters.toArray(Frame[]::new));

		return builder.toFrame();
	}

	private static String expressionOf(Expression expr) {
		return decorateExpr(rawExpressionOf(expr));
	}

	private static String decorateExpr(String expr) {
		expr = expr.trim();
		if(StringUtils.countMatches(expr, "\n") == 0) {
			if(!expr.startsWith("return")) expr = "return " + expr;
			if(!expr.endsWith(";")) expr += ";";
			return expr;
		}
		String[] lines = expr.split("\n", -1);
		return Arrays.stream(lines).map(ExpressionHelper::removeDefaultItrIndentation).collect(Collectors.joining("\n"));
	}

	private static String removeDefaultItrIndentation(String line) {
		return !line.startsWith(DEFAULT_ITR_INDENTATION) ? line : line.substring(DEFAULT_ITR_INDENTATION.length());
	}

	private static String rawExpressionOf(Expression expr) {
		try {
			List<?> expression = expr.core$().variables().get("expression");
			return (String) expression.get(0);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot read expression of " + expr.name$());
		}
	}

	private static List<Frame> parametersOf(Expression expr) {
		if(expr.isGetter()) return Collections.emptyList();
		return expr.core$().componentList().stream()
				.filter(c -> c.is(Expression.Function.Parameter.class) || c.is(Expression.Routine.Parameter.class))
				.map(ExpressionHelper::frameOfParameter)
				.collect(Collectors.toList());
	}

	private static Frame frameOfParameter(Node c) {
		FrameBuilder builder = new FrameBuilder("parameter");
		String type = javaName(type(c), c);
		builder.add("type", type);
		builder.add("name", c.name());
		return builder.toFrame();
	}

	private static String javaName(String type, Node node) {
		Parameter entity = parameter(node, "entity");
		if (entity != null) {
			return ((Entity) entity.values().get(0)).name$();
		}

		Parameter struct = parameter(node, "struct");
		if (struct != null) {
			Node structNode = ((Struct) struct.values().get(0)).core$();
			return structNode.name();
		}

		return type;
	}

	private static String returnTypeOf(Expression expr) {
		if(expr.isFunction()) return javaName(type(expr.asFunction().returnType().core$()), expr.asFunction().returnType().core$());
		if(expr.isDoubleGetter()) return "double";
		if(expr.isIntegerGetter()) return "int";
		if(expr.isLongGetter()) return "long";
		if(expr.isBooleanGetter()) return "boolean";
		if(expr.isStringGetter()) return "String";
		if(expr.isDateGetter()) return "LocalDate";
		if(expr.isDateTimeGetter()) return "LocalDateTime";
		if(expr.isInstantGetter()) return "Instant";
		return "void";
	}

	private static Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private static String type(Node node) {
		String aspect = node.conceptList().stream().map(Concept::name).filter(ExpressionHelper::isProperTypeName).findFirst().orElse("");

		boolean list = node.conceptList().stream().anyMatch(a -> a.name().equals("List"));
		if (list) return ListTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");

		boolean set = node.conceptList().stream().anyMatch(a -> a.name().equals("Set"));
		if (set) return SetTypes.getOrDefault(aspect, "Set<" + firstUpperCase().format(node.name()).toString() + ">");

		return TheTypes.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
	}

	private static boolean isProperTypeName(String s) {
		return !s.equals("Set") && !s.equals("List") && !s.equals("Optional") && !s.equals("Type") && !s.equals("Required");
	}

}
