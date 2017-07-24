package io.intino.ness.datalake.compiler;

import io.intino.ness.inl.Message;

@FunctionalInterface
public interface TextMapper {
	Message map(String input);
}
