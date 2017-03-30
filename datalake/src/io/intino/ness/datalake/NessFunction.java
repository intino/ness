package io.intino.ness.datalake;

import io.intino.ness.inl.Message;

public interface NessFunction {
    Message cast(Message input);

}
