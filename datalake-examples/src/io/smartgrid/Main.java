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
            output.write("ts", ts(input));
            output.write("temperature", input.read("TE"));
            output.write("windDirection", input.read("WR"));
            output.write("windSpeed", input.read("WG"));
            output.write("light", input.read("LX"));
            output.write("radiation", input.read("SO"));
            output.write("pressure", input.read("DR"));
            output.write("humidity", input.read("FE"));
            return output;
        }
    }

    public static class ImportHeaterMapper implements MessageMapper {
        @Override
        public Message map(Message input) {
            Message output = new Message("Heater");
            output.write("ts", ts(input));
            output.write("energy", input.read("ene"));
            output.write("vol", input.read("vol"));
            output.write("inTemp", input.read("vlt"));
            output.write("outTemp", input.read("rlt"));
            output.write("building", input.read("bld"));
            output.write("room", input.read("node"));
            output.write("roomName", input.read("id"));
            return output;
        }

    }
    public static class ImportPowerConsumptionMapper implements MessageMapper {

        @Override
        public Message map(Message input) {
            Message output = new Message("PowerConsumption");
            output.write("ts", ts(input));
            output.write("activePower", input.read("Global_active_power"));
            output.write("reactivePower", input.read("Global_reactive_power"));
            output.write("intensity", input.read("Global_intensity"));
            output.write("voltage", input.read("Voltage"));
            output.write("kitchenPower", input.read("Sub_metering_1"));
            output.write("laundryPower", input.read("Sub_metering_2"));
            output.write("hvacPower", input.read("Sub_metering_3"));
            return output;
        }

        private String ts(Message input) {
            String[] date = input.read("date").split("/");
            String time = input.read("time");
            return date[2] + "-" + zero(date[1]) + "-" + zero(date[0]) + "T" + time + "Z";
        }

        private String zero(String s) {
            return ("0" +s).substring(s.length()-1,s.length()+1);
        }

    }
    private static String ts(Message message) {
        return ts(message.read("Date"),message.read("Time"));
    }

    private static String ts(String date, String time) {
        String[] split = date.split("_");
        return split[2] + "-" + split[1] + "-" + split[0] + "T" + time + "Z";
    }

}
