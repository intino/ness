package io.intino.ness.inl;

public interface MessageFilter extends MessageFunction {
    boolean filter(Message message);
}
