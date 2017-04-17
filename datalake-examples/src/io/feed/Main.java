package io.feed;

import io.intino.ness.datalake.FilePumpingStation;
import io.intino.ness.datalake.NessPumpingStation;
import io.intino.ness.datalake.NessPumpingStation.Pipe;
import io.intino.ness.datalake.Pipes;
import io.intino.ness.inl.FileMessageInputStream;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFunction;
import io.intino.ness.inl.MessageInputStream;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static io.intino.ness.datalake.Pipes.csv;
import static io.intino.ness.datalake.Pipes.out;
import static io.intino.ness.datalake.Pipes.progress;
import static java.lang.Thread.currentThread;

public class Main {

    public static void main(String[] args) throws Exception {
        clean();
        feedWithPipeToConsoleAndCsv();
        System.out.println("-------------------");
        pumpWithPipeToProgress();
        System.out.println("-------------------");
        pumpWithPipeToChannel();
        System.out.println("-------------------");
        pumpWithPipeToCsv();
    }

    private static void pumpWithPipeToCsv() throws Exception {
        NessPumpingStation station = createStation();
        Pipe pipe = station.pipe("channel.weather.Temperature.1")
                .to(csv("temperatures.csv"));
        station.pump("channel.weather.Temperature.1").thread().join();

        station.close(pipe);
    }

    private static void pumpWithPipeToChannel() throws Exception {
        NessPumpingStation station = createStation();
        station.create("channel.weather.Temperature.2");
        Pipe pipe = station.pipe("channel.weather.Temperature.1")
                .with(ToFarenheit.class)
                .to("channel.weather.Temperature.2");
        station.pump("channel.weather.Temperature.1").thread().join();
        station.close(pipe);
    }

    private static void pumpWithPipeToProgress() throws Exception {
        NessPumpingStation station = createStation();
        Pipe pipe = station.pipe("channel.weather.Temperature.1").to(progress(100));
        station.pump("channel.weather.Temperature.1").thread().join();
        station.close(pipe);
    }

    private static void feedWithPipeToConsoleAndCsv() throws URISyntaxException, IOException, InterruptedException {
        NessPumpingStation station = createStation();
        station.create("channel.weather.Temperature.1");
        Pipe feed = station.feed("channel.weather.Temperature.1");
        Pipe pipe = station.pipe("channel.weather.Temperature.1").to(out, csv("temperature.csv"));

        MessageInputStream messages = messages();
        for (int i = 0; i < 10; i++) feed.send(messages.next());
        station.seal("channel.weather.Temperature.1").thread().join();
        for (int i = 0; i < 9; i++) feed.send(messages.next());
        station.seal("channel.weather.Temperature.1").thread().join();

        pipe.flush();
        station.close(pipe);
        station.close(feed);
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
        URL resource = classLoader().getResource("temperatures.inl");
        assert resource != null;
        return FileMessageInputStream.of(new File(resource.toURI()));
    }


    private static ClassLoader classLoader() {
        return currentThread().getContextClassLoader();
    }

    public static class ToFarenheit implements MessageFunction {

        @Override
        public Message cast(Message input) {
            double value = input.parse("value").as(double.class);
            input.write("value", value * 9.0/5 + 32);
            return input;
        }
    }

}
