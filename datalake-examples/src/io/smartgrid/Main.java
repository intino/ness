package io.smartgrid;

import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.toolbox.Import;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;

public class Main {

    public static void main(String[] args) throws Exception {
        NessStation station = new FileStation("datalake-examples/local.store");


        Thread t1 = Import.from("datalake-examples/local.legacy/smart-heater")
                    .map(new ImportHeaterMapper())
                    .to(station.feed("tank.smartgrid.Heater.1")).thread();


        Thread t2 = Import.from("datalake-examples/local.legacy/weather")
                    .map(new ImportWeatherMapper())
                    .to(station.feed("tank.smartgrid.Temperature.1")).thread();


        Thread t3 = Import.from("datalake-examples/local.legacy/power-consumption")
                    .map(new ImportPowerConsumptionMapper())
                    .to(station.feed("tank.smartgrid.PowerConsumption.1")).thread();


        t1.join();
        t2.join();
        t3.join();

    }

    public static class ImportWeatherMapper implements MessageMapper {
        @Override
        public Message map(Message input) {
            //http://process.monitorering.no/measureit/files/pdf_datablad/reinhardt/reinhardt_mws9-5_manual.pdf
            Message output = new Message("Weather");
            output.set("ts", ts(input));
            output.set("temperature", input.get("TE"));
            output.set("windDirection", input.get("WR"));
            output.set("windSpeed", input.get("WG"));
            output.set("light", input.get("LX"));
            output.set("radiation", input.get("SO"));
            output.set("pressure", input.get("DR"));
            output.set("humidity", input.get("FE"));
            return output;
        }
    }

    public static class ImportHeaterMapper implements MessageMapper {
        @Override
        public Message map(Message input) {
            Message output = new Message("Heater");
            output.set("ts", ts(input));
            output.set("energy", input.get("ene"));
            output.set("vol", input.get("vol"));
            output.set("inTemp", input.get("vlt"));
            output.set("outTemp", input.get("rlt"));
            output.set("building", input.get("bld"));
            output.set("room", input.get("node"));
            output.set("roomName", input.get("id"));
            return output;
        }

    }
    public static class ImportPowerConsumptionMapper implements MessageMapper {

        @Override
        public Message map(Message input) {
            Message output = new Message("PowerConsumption");
            output.set("ts", ts(input));
            output.set("activePower", input.get("Global_active_power"));
            output.set("reactivePower", input.get("Global_reactive_power"));
            output.set("intensity", input.get("Global_intensity"));
            output.set("voltage", input.get("Voltage"));
            output.set("kitchenPower", input.get("Sub_metering_1"));
            output.set("laundryPower", input.get("Sub_metering_2"));
            output.set("hvacPower", input.get("Sub_metering_3"));
            return output;
        }

        private String ts(Message input) {
            String[] date = input.get("date").split("/");
            String time = input.get("time");
            return date[2] + "-" + zero(date[1]) + "-" + zero(date[0]) + "T" + time + "Z";
        }

        private String zero(String s) {
            return ("0" +s).substring(s.length()-1,s.length()+1);
        }

    }
    private static String ts(Message message) {
        return ts(message.get("Date"),message.get("Time"));
    }

    private static String ts(String date, String time) {
        String[] split = date.split("_");
        return split[2] + "-" + split[1] + "-" + split[0] + "T" + time + "Z";
    }

}
