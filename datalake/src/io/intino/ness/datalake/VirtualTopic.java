package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Reservoir;
import io.intino.ness.datalake.NessDataLake.Topic;

import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class VirtualTopic implements Topic {
    private final Topic topic;
    private final List<Reservoir> reservoirs;

    public VirtualTopic(Topic topic, NessFunction function) {
        this.topic = topic;
        this.reservoirs = topic.reservoirs().stream()
                .map(r->new VirtualReservoir(r, function))
                .collect(toList());
    }

    @Override
    public String name() {
        return topic.name();
    }

    @Override
    public List<Reservoir> reservoirs() {
        return reservoirs;
    }

    @Override
    public Reservoir get(Instant ts) {
        return null;
    }

}
