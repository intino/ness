package io.intino.ness.core.functions;

import io.intino.ness.inl.Message;

public interface MessageMapper extends MessageFunction {
    Message map(Message input);
}
