package io.gemini;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import io.intino.ness.Inl;
import io.intino.ness.datalake.Feeder;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.Job;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.inl.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.time.Instant.now;

public class Main {

    private static final Map<String,String> actions = new HashMap<>();
    private static Feed ask;
    private static Feed bid;

    public static void main(String[] args) throws IOException, WebSocketException {
        NessStation station = new FileStation("datalake-examples/local.lake");
        bid = station.feed("channel.humket.Bid");
        ask = station.feed("channel.humket.Ask");
        listen("btc");
        listen("eth");
    }

    private static WebSocket listen(String market) throws WebSocketException, IOException {
        return new WebSocketFactory()
                .createSocket("wss://api.gemini.com/v1/marketdata/"+market+"usd")
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket ws, String text) {                          
                        if (text.contains("initial")) return;
                        String line = header(text)
                                + "\n" + clean(text)
                                + "\nexchange:gemini"
                                + "\nmarket:" + market + "\n";
                        Message message = Inl.load(line).get(0);
                        if (message.is("Bid")) bid.send(message);
                        if (message.is("Ask")) ask.send(message);
                    }
                })
                .connect();
    }

    private static String header(String text) {
        return type(text) + "\n" + "ts:" + now().toString() + "\naction:" + action(text);
    }

    private static String type(String text) {
        if (text.contains("bid")) return "[Bid]";
        if (text.contains("ask")) return "[Ask]";
        return "[???]";
    }


    private static String action(String text) {
        for (String key : actions.keySet())
            if (text.contains(key)) return actions.get(key);
        return "???";
    }

    private static String clean(String text) {
        return text.replace("\"","")
                .replaceAll("\\{type:update.*\\{type:change,", "")
                .replaceAll("side:...,","")
                .replaceAll(",reason.*", "")
                .replace("delta:","amount:")
                .replace(",","\n");
    }


    static {
        actions.put("place","place");
        actions.put("cancel","cancel");
        actions.put("trade","trade");
    }

}
