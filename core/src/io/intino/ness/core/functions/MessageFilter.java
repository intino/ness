package io.intino.ness.core.functions;

import io.intino.ness.inl.Message;

public interface MessageFilter extends MessageFunction {
    boolean filter(Message message);
}
