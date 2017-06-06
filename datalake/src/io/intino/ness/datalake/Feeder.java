package io.intino.ness.datalake;

import io.intino.ness.datalake.NessStation.Feed;

public interface Feeder {

    Job to(Feed feed);

}
