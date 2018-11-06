package io.intino.ness.core.functions;

import io.intino.ness.inl.Message;

public interface TextMapper extends MessageFunction {
	Message map(String input);
}
