import io.intino.ness.datalake.*;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.NessStation.Flow;
import io.intino.ness.datalake.NessStation.Pipe;
import io.intino.ness.inl.Formats;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

public class FileStation_ {

    private static String temperature_1 = "tank.weather.Temperature.1";
    private static String temperature_2 = "tank.weather.Temperature.2";

    private NessStation station;

    @Before
    public void setUp() throws Exception {
        remove(new File("local.lake"));
        station = new FileStation("local.lake");
    }

    @Test
    public void should_create_channel() throws Exception {
        station.tank(temperature_1);
        assertThat(station.tanks().length, is(1));
        assertThat(station.tanks()[0].name(), is(temperature_1));
    }

    @Test
    public void should_remove_channel() throws Exception {
        station.tank(temperature_1);
        station.remove(temperature_1);
        assertThat(station.tanks().length, is(1));
        assertThat(station.tanks()[0].name().startsWith("trash"), is(true));
        assertThat(station.tanks()[0].name().endsWith(temperature_1), is(true));
    }

    @Test
    public void should_throw_exception_when_creating_a_channel_that_already_exists() throws Exception {
        station.tank(temperature_1);
        try {
            station.tank(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_exception_when_removing_a_channel_that_does_not_exist() throws Exception {
        try {
            station.remove(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_exception_when_renaming_a_channel_that_does_not_exist() throws Exception {
        try {
            station.rename(temperature_1, temperature_2);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_create_feed_to_channel() throws Exception {
        assertThat(station.feedsTo(temperature_1).length, is(0));
        station.tank(temperature_1);
        station.feed(temperature_1);
        assertThat(station.feedsTo(temperature_1).length, is(1));
    }

    @Test
    public void should_create_flow_from_channel() throws Exception {
        assertThat(station.flowsFrom(temperature_1).length, is(0));
        station.tank(temperature_1);
        station.flow(temperature_1);
        assertThat(station.flowsFrom(temperature_1).length, is(1));
    }

    @Test
    public void should_throw_an_exception_when_creating_a_feed_to_a_channel_that_does_not_exist() throws Exception {
        try {
            station.feed(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_an_exception_when_creating_a_flow_from_a_channel_that_does_not_exist() throws Exception {
        try {
            station.flow(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_create_pipe() throws Exception {
        assertThat(station.pipesFrom(temperature_1).length, is(0));
        assertThat(station.pipesTo(temperature_2).length, is(0));
        station.tank(temperature_1);
        station.tank(temperature_2);
        station.pipe(temperature_1).to(temperature_2);
        assertThat(station.pipesFrom(temperature_1).length, is(1));
        assertThat(station.pipesTo(temperature_2).length, is(1));
        assertThat(station.pipeBetween(temperature_1, temperature_2), is(notNullValue()));
    }

    @Test
    public void should_throw_an_exception_when_creating_a_pipe_from_a_source_that_does_not_exists() throws Exception {
        try {
            station.pipe(temperature_1).to(temperature_2);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_an_exception_when_creating_a_pipe_to_a_target_that_does_not_exists() throws Exception {
        try {
            station.tank(temperature_1);
            station.pipe(temperature_1).to(temperature_2);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_exception_when_removing_a_channel_with_a_flow() throws Exception {
        station.tank(temperature_1);
        station.flow(temperature_1).to(System.out::println);
        try {
            station.remove(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_throw_exception_when_renaming_a_channel_with_a_feed() throws Exception {
        station.tank(temperature_1);
        station.feed(temperature_1);
        try {
            station.remove(temperature_1);
            assertThat("Exception was not thrown", false);
        }
        catch (Exception e) {
            assertThat("Exception was thrown", true);
        }
    }

    @Test
    public void should_flow_feed() throws Exception {
        List<Message> messages = new ArrayList<>();
        station.tank(temperature_1);
        Flow flow = station.flow(temperature_1).to(messages::add);
        feedTemperatures(0,19);
        station.remove(flow);

        assertThat(messages.size(), is(19));
        assertThat(station.flowsFrom(temperature_1).length,is(0));
        assertThat(station.feedsTo(temperature_1).length,is(1));
    }

    @Test
    public void should_seal_a_tank() throws Exception {
        Tank tank = station.tank(temperature_1);
        feedTemperatures(0,19);
        station.seal(temperature_1).thread().join();
        checkTank(tank);
    }

    @Test
    public void should_seal_a_tank_already_sealed() throws Exception {
        Tank tank = station.tank(temperature_1);
        feedTemperatures(0,10);
        station.seal(temperature_1).thread().join();
        feedTemperatures(11,19);
        station.seal(temperature_1).thread().join();
        checkTank(tank);
    }

    @Test
    public void should_pump_not_sealed_tank() throws Exception {
        station.tank(temperature_1);
        feedTemperatures(0,19);

        Tank tank = station.tank(temperature_2);
        station.pipe(temperature_1).with(Valve.define().map(toFarenheit())).to(temperature_2);
        station.pump(temperature_1).to(temperature_2).start().thread().join();

        checkTank(tank);
    }

    @Test
    public void should_pump_sealed_tank() throws Exception {
        station.tank(temperature_1);
        feedTemperatures(0,19);
        station.seal(temperature_1).thread().join();

        Tank tank = station.tank(temperature_2);
        station.pipe(temperature_1).with(Valve.define().map(toFarenheit())).to(temperature_2);
        station.pump(temperature_1).to(temperature_2).start().thread().join();

        checkTank(tank);
    }

    @Test
    public void should_pump_partially_sealed_tank() throws Exception {
        station.tank(temperature_1);
        feedTemperatures(0,5);
        station.seal(temperature_1).thread().join();
        feedTemperatures(6,19);

        Tank tank = station.tank(temperature_2);
        station.pipe(temperature_1).with(Valve.define().map(toFarenheit())).to(temperature_2);
        station.pump(temperature_1).to(temperature_2).start().thread().join();

        checkTank(tank);
    }

    @Test
    public void should_reflow_from_tank() throws Exception {
        List<Message> messages = new ArrayList<>();
        station.tank(temperature_1);
        feedTemperatures(0,19);
        station.pump(temperature_1).to(messages::add).start().thread().join();
        assertThat(messages.size(), is(19));
    }

    @Test
    public void feed_usage() throws Exception {
        station.tank(temperature_1);
        station.tank(temperature_2);
        Feed feed = station.feed(temperature_1);
        Pipe pipe = station.pipe(temperature_1)
                .with(valve())
                .to(temperature_2);
        Flow flow = station.flow(temperature_2).to(post());

        assertThat(station.feedsTo(temperature_1).length, is(1));
        assertThat(station.pipesFrom(temperature_1).length, is(1));
        assertThat(station.pipesFrom(temperature_2).length, is(0));
        assertThat(station.pipesTo(temperature_2).length, is(1));
        assertThat(station.pipeBetween(temperature_1,temperature_2), is(notNullValue()));
        assertThat(station.flowsFrom(temperature_1).length, is(0));
        assertThat(station.flowsFrom(temperature_2).length, is(1));
        assertThat(feed.toString(), is("feed > tank.weather.Temperature.1"));
        assertThat(pipe.toString(), is("tank.weather.Temperature.1 > [2] > tank.weather.Temperature.2"));
        assertThat(flow.toString(), is("tank.weather.Temperature.2 > flow"));
    }

    private void checkTank(Tank tank) throws IOException {
        assertThat(tank.tubs().length, is(4));
        assertThat(tank.tubs()[0].name(), is("20101112.zip"));
        assertThat(tank.tubs()[0].input().next().ts(), is("2010-11-12T03:36:00Z"));
        checkTubSize(tank.tubs()[0].input(), 5);
        assertThat(tank.tubs()[1].name(), is("20101113.zip"));
        assertThat(tank.tubs()[1].input().next().ts(), is("2010-11-13T01:12:00Z"));
        checkTubSize(tank.tubs()[1].input(), 5);
        assertThat(tank.tubs()[2].name(), is("20101114.zip"));
        assertThat(tank.tubs()[2].input().next().ts(), is("2010-11-14T04:00:00Z"));
        checkTubSize(tank.tubs()[2].input(), 5);
        assertThat(tank.tubs()[3].name(), is("20101115.zip"));
        assertThat(tank.tubs()[3].input().next().ts(), is("2010-11-15T16:48:00Z"));
        checkTubSize(tank.tubs()[3].input(), 4);
    }

    private void checkTubSize(MessageInputStream input, int size) throws IOException {
        for (int i = 0; i < size; i++) assertThat(input.next(), is(notNullValue()));
        assertThat(input.next(), is(nullValue()));
        assertThat(input.next(), is(nullValue()));

    }

    private MessageMapper toFarenheit() {
        return message -> message.write("value", message.parse("value").as(Double.class)* 9/5.0 + 32);
    }

    private void feedTemperatures(int init, int finish) throws Exception {
        int index = 0;
        Feed feed = station.feed(temperature_1);
        MessageInputStream input = messages();
        while (index <= finish) {
            Message message = input.next();
            if (message == null) break;
            if (index >= init) feed.send(message);
        }
    }

    private Valve valve() throws Exception {
        return Valve.define().filter(m->m.is("xxx")).filter(m->m.length() > 10);
    }

    private Post post() {
        return message -> System.out.println(message.toString());
    }

    private static MessageInputStream messages() throws URISyntaxException, IOException {
        InputStream stream = classLoader().getResourceAsStream("temperatures.inl");
        return Formats.Inl.of(stream);
    }

    private static ClassLoader classLoader() {
        return currentThread().getContextClassLoader();
    }

    private void remove(File file) {
        File[] contents = file.listFiles();
        if (contents != null) for (File f : contents) remove(f);
        file.delete();
    }


}
