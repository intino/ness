import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.NessStation.Flow;
import io.intino.ness.datalake.NessStation.Pipe;
import io.intino.ness.datalake.Post;
import io.intino.ness.datalake.Valve;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilePumpingStation_ {

    @Test
    public void feed_usage() throws Exception {
        NessStation station = new FileStation("local.store");
        Feed feed = station.feed("channel.weather.Temperature.1");
        Pipe pipe = station.pipe("channel.weather.Temperature.1")
                .with(valve())
                .to("channel.weather.Temperature.2");
        Flow flow = station.flow("channel.weather.Temperature.2").onMessage(post());

        assertThat(station.feedsTo("channel.weather.Temperature.1").size(), is(1));
        assertThat(station.pipesFrom("channel.weather.Temperature.1").size(), is(1));
        assertThat(station.pipesFrom("channel.weather.Temperature.2").size(), is(0));
        assertThat(station.pipesTo("channel.weather.Temperature.2").size(), is(1));
        assertThat(station.pipesBetween("channel.weather.Temperature.1","channel.weather.Temperature.2").size(), is(1));
        assertThat(station.flowsFrom("channel.weather.Temperature.1").size(), is(0));
        assertThat(station.flowsFrom("channel.weather.Temperature.2").size(), is(1));
        assertThat(feed.toString(), is("feed > channel.weather.Temperature.1"));
        assertThat(pipe.toString(), is("channel.weather.Temperature.1 > [2] > channel.weather.Temperature.2"));
        assertThat(flow.toString(), is("channel.weather.Temperature.2 > flow"));
    }

    @Test
    public void pump_usage() throws Exception {
        NessStation station = new FileStation("local.store");
        station.pipe("channel.weather.Temperature.1")
                .to("channel.weather.Temperature.2")
                .with(valve());
        station.pump("channel.weather.Temperature.1")
                .to("channel.weather.Temperature.2")
                .start().thread().join();
    }


    @Test
    public void reflow() throws Exception {
        NessStation station = new FileStation("local.store");
        station.flow(
        station.pump("channel.weather.Temperature.1").to(post());
    }

    @Test
    public void seal_usage() throws Exception {
        NessStation station = new FileStation("local.store");
        station.seal("channel.weather.Temperature.1");
    }


    private Valve valve() throws Exception {
        return Valve.define().filter(m->m.is("xxx")).filter(m->m.length() > 10);
    }

    private Post post() {
        return message -> System.out.println(message.toString());
    }
}
