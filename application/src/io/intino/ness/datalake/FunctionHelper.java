package io.intino.ness.datalake;

import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.graph.Function;
import io.intino.ness.inl.MessageFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class FunctionHelper {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	public static boolean check(String className, String code) {
		try {
			return compile(className, code) != null;
		} catch (Compiler.Exception e) {
			return false;
		}
	}

	private static MessageFunction compile(String className, String code) {
		try {
			return Compiler.
					compile(code).
					with("-target", "1.8").
					load(className).
					as(MessageFunction.class).
					newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			return null;
		}
	}



	public static MessageFunction map(Function function) {
		return map(function.qualifiedName(), function.source());
	}

	public static MessageFunction map(String function, String... sources) {
		try {
			return map(compile(function, sources).as(MessageFunction.class));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static Compiler.Result compile(String function, String... sources) {
		return Compiler.compile(sources)
				.with("-target", "1.8")
				.load(function);
	}

	private static MessageFunction map(Class<? extends MessageFunction> mapperClass) throws Exception {
		return mapperClass.newInstance();
	}
}
