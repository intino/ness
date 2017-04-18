package io.feed;

import io.intino.ness.datalake.FilePumpingStation;
import io.intino.ness.datalake.NessPumpingStation;
import io.intino.ness.datalake.NessPumpingStation.Pipe;
import io.intino.ness.inl.Formats;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.MessageMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static io.intino.ness.datalake.Pipes.csv;
import static io.intino.ness.datalake.Probes.out;
import static io.intino.ness.datalake.Probes.progress;
import static java.lang.Thread.currentThread;

public class Main {

    public static void main(String[] args) throws Exception {
        clean();
        feedToFlow();
        exportChannelToCsv();
    }

    private static void feedToFlow() throws Exception {
        NessPumpingStation station = createStation();

        station.create("channel.weather.Temperature.1");
        Pipe feed = station
                .pipe()
                .map(out)
                .to("channel.weather.Temperature.1");

        Pipe flow = station
                .pipe("channel.weather.Temperature.1")
                .map(progress(1))
                .to(flow());

        MessageInputStream messages = messages();
        for (int i = 0; i < 10; i++) feed.send(messages.next());
        station.seal("channel.weather.Temperature.1").thread().join();
        station.close(flow);

        for (int i = 0; i < 9; i++) feed.send(messages.next());
        station.seal("channel.weather.Temperature.1").thread().join();

        station.close(feed);
    }

    private static void exportChannelToCsv() throws Exception {
        NessPumpingStation station = createStation();
        Pipe pipe = station.pipe("channel.weather.Temperature.1")
                .map(ToFahrenheit.class)
                .to(csv("temperatures.csv"));
        station.pump("channel.weather.Temperature.1").thread().join();
        // El pipe se destruye al terminar el bombeo
    }

    private static void channelToChannelAndFeed() throws Exception {
        NessPumpingStation station = createStation();
        station.create("channel.weather.Temperature.2");
        station.pipe("channel.weather.Temperature.1")
                .map(ToFahrenheit.class)
                .to("channel.weather.Temperature.2");
        station.pump("channel.weather.Temperature.1").thread().join();
    }

    private static void channelToFlow() throws Exception {
        NessPumpingStation station = createStation();
        station.pipe("channel.weather.Temperature.1")
                .map(progress(100))
                .to(flow());
        station.pump("channel.weather.Temperature.1").thread().join();

    }

    private static Pipe flow() {
        return message -> System.out.println(message.length());
    }

    private static void clean() {
        NessPumpingStation station = createStation();

        if (station.exists("channel.weather.Temperature.1"))
            station.remove("channel.weather.Temperature.1");

        if (station.exists("channel.weather.Temperature.2"))
            station.remove("channel.weather.Temperature.2");
    }

    private static NessPumpingStation createStation() {
        File file = new File("datalake-examples/local.store");
        return new FilePumpingStation(file);
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
