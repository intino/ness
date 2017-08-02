package io.intino.ness.datalake;

import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.inl.MessageFunction;

public class FunctionManager {


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
}
