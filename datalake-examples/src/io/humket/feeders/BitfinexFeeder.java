package io.humket.bitfinex;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws IOException, WebSocketException {
        String[] subscriptions = {
            "{ \"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"BTCUSD\",\"prec\":\"P0\",\"freq\":\"F0\" }"
            "{ \"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"LTCUSD\",\"prec\":\"P0\",\"freq\":\"F0\" }",
            "{ \"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"LTCBTC\",\"prec\":\"P0\",\"freq\":\"F0\" }",
            "{ \"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"ETHUSD\",\"prec\":\"P0\",\"freq\":\"F0\" }",
            "{ \"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"ETHBTC\",\"prec\":\"P0\",\"freq\":\"F0\" }"
        };
        WebSocket webSocket = new WebSocketFactory()
                .createSocket("wss://api2.bitfinex.com:3000/ws")
                .addListener(new WebSocketAdapter() {
                    private Map<Integer, String> channels = new HashMap<>();
                    private Map<String, Integer> slots = new HashMap<>();

                    @Override
                    public void onTextMessage(WebSocket ws, String text) {
                        if (text.contains("error")) ws.sendClose();
                        else if (text.contains("info") || text.contains("hb")) return;
                        else if (text.contains("subscribe")) subscribe(text);
                        else if (text.contains("]]]")) init(text);
                        else feed(text);

                    }

                    private void subscribe(String text) {
                        String[] split = text.replaceAll("\"", "")
                                .replace("}", "")
                                .split("[:,]");
                        channels.put(Integer.parseInt(split[5]),split[13]);
                    }

                    private void init(String text) {
                        Parser parser = Parser.of(text);
                        String channel = channels.get(parser.nextInt());
                        while (parser.hasNext()) {
                            set(channel + "." + parser.nextDouble(), parser.nextInt());
                            parser.nextDouble();
                        }
                    }

                    private void feed(String text) {
                        Parser parser = Parser.of(text);
                        String channel = channels.get(parser.nextInt());
                        double price = parser.nextDouble();
                        int count = parser.nextInt();
                        double amount = parser.nextDouble();
                        String slot = channel + "." + price;
                        String message = line((amount > 0 ? "[Bid]" : "[Ask]"))
                                + line("ts: " + Instant.now().toString())
                                + line("action: " + (count > get(slot) ? "place" : "cancel"))
                                + line("price: " + price)
                                + line("amount: " + Math.abs(amount))
                                + line("market: " + channel.substring(0,3));
                        set(slot, count);
                        System.out.println(message);
                    }

                    private String line(String s) {
                        return s + "\n";
                    }

                    private void set(String key, int count) {
                        System.out.println(key + " " + count);
                        if (count == 0)
                            slots.remove(key);
                        else
                            slots.put(key, count);
                    }

                    private int get(String key) {
                        return slots.getOrDefault(key, 0);
                    }
                });
        webSocket.connect();
        for (String subscription : subscriptions) {
            webSocket.sendText(subscription);
        }

    }

    private static class Parser {

        private char[] chars;
        private int i = 0;

        public Parser(char[] chars) {
            this.chars = chars;
        }

        public static Parser of(String text) {
            return new Parser(text.toCharArray());
        }

        public boolean hasNext() {
            while (i < chars.length) {
                if (isDigit() || isMinus()) return true;
                i++;
            }
            return false;

        }

        public int nextInt() {
            if (!hasNext()) return Integer.MAX_VALUE;
            StringBuilder result = new StringBuilder();
            while (isDigit()) result.append(chars[i++]);
            return Integer.parseInt(result.toString());
        }

        public double nextDouble() {
            if (!hasNext()) return Double.MAX_VALUE;
            StringBuilder result = new StringBuilder();
            if (isMinus()) result.append(chars[i++]);
            while (isDigit() || isDot()) result.append(chars[i++]);
            return Double.parseDouble(result.toString());
        }

        private boolean isDigit() {
            return Character.isDigit(chars[i]);
        }

        public boolean isDot() {
            return chars[i] == '.';
        }

        public boolean isMinus() {
            return chars[i] == '-';
        }
    }

}
