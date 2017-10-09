package io.intino.ness;

import io.intino.ness.inl.MessageInputStream;

public interface Joint {
	MessageInputStream join(MessageInputStream[] inputStreams);
}
