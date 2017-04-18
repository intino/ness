package io.intino.ness.inl;

public interface MessageMapper extends MessageFunction {
    Message map(Message input);
}
