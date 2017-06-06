package io.feed;

import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.NessStation.Flow;
import io.intino.ness.datalake.NessStation.Pipe;
import io.intino.ness.datalake.Valve;
import io.intino.ness.inl.Formats;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static io.intino.ness.datalake.toolbox.Posts.*;
import static java.lang.Thread.currentThread;

public class Main {

    private static final String temperature_1 = "tank.weather.Temperature.1";
    private static final String temperature_2 = "tank.weather.Temperature.2";

    public static void main(String[] args) throws Exception {
        MessageInputStream messages = messages();
        NessStation station = createStation();

        Feed feed = station.feed(temperature_1);
        Flow flow = station.flow(temperature_1).to(out);
        Pipe pipe = station.pipe(temperature_1)
                .with(Valve.define().map(ToFahrenheit.class))
                .to(temperature_2);


        for (int i = 0; i < 10; i++) feed.send(messages.next());
        station.seal(temperature_1).thread().join();

        station.remove(flow);
        station.flow(temperature_2).to(console);

        for (int i = 0; i < 9; i++) feed.send(messages.next());
        station.seal(temperature_1).thread().join();

        station.pump(temperature_1).to(file("temperatures.csv"));
        station.pump(temperature_1).start().thread().join();
    }


    private static NessStation clean(FileStation station) {
        if (station.exists(temperature_1)) station.remove(temperature_1);
        station.tank(temperature_1);

        if (station.exists(temperature_2)) station.remove(temperature_2);
        station.tank(temperature_2);

        return station;
    }

    private static NessStation createStation() {
        File file = new File("datalake-examples/local.store");
        return clean(new FileStation(file));
    }

    private static MessageInputStream messages() throws URISyntaxException, IOException {
        InputStream stream = classLoader().getResourceAsStream("temperatures.inl");
        return Formats.Inl.of(stream);
    }


    private static ClassLoader classLoader() {
        return currentThread().getContextClassLoader();
    }

    public static class ToFahrenheit implements MessageMapper {

        @Override
        public Message map(Message input) {
            double value = input.parse("value").as(double.class);
            input.write("value", value * 9.0/5 + 32);
            return input;
        }
    }

}
