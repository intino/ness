package io.intino.ness.datalake;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.functions.MessageFunction;
import io.intino.ness.datalake.compiler.Compiler;

public class FunctionHelper {

	public static boolean check(String className, String code) {
		try {
			return compile(className, code) != null;
		} catch (Compiler.Exception e) {
			Logger.error(e.getMessage(), e);
			return false;
		}
	}

	public static MessageFunction compile(String className, String code) {
		try {
			return Compiler.
					compile(code).
					with("-target", "1.8").
					load(className).
					as(MessageFunction.class).
					newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

}
