package io.smartgrid;

import io.intino.ness.datalake.NessDataLake;
import io.intino.ness.datalake.NessFunction;
import io.intino.ness.datalake.NessPump;
import io.intino.ness.datalake.FileDataLake;
import io.intino.ness.inl.Message;

public class Main {

    public static void main(String[] args) throws Exception {
        NessDataLake dataLake = new FileDataLake("datalake-examples/local.store");
        NessPump pump = new NessPump(dataLake);
        pump.plug("legacy.smartgrid.Heater")
            .with(ImportHeaterFunction.class)
            .into("feed.smartgrid.Heater.1");
        pump.plug("legacy.smartgrid.Weather")
            .with(ImportWeatherFunction.class)
            .into("feed.smartgrid.Temperature.1");
        pump.plug("legacy.smartgrid.Weather")
            .with(ImportWindFunction.class)
            .into("feed.smartgrid.Wind.1");
        pump.plug("legacy.smartgrid.PowerConsumption")
            .with(ImportPowerConsumptionFunction.class)
            .into("feed.smartgrid.PowerConsumption.1");
        pump.execute().thread().join();

    }

    public static class ImportWeatherFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            //http://process.monitorering.no/measureit/files/pdf_datablad/reinhardt/reinhardt_mws9-5_manual.pdf
            Message output = new Message("Weather");
            output.write("ts", ts(input));
            output.write("temperature", input.parse("TE").as(String.class));
            output.write("light", input.parse("LX").as(String.class));
            output.write("radiation", input.parse("SO").as(String.class));
            output.write("pressure", input.parse("DR").as(String.class));
            output.write("humidity", input.parse("FE").as(String.class));
            return output;
        }
    }

    public static class ImportWindFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            Message output = new Message("Wind");
            output.write("ts", ts(input));
            output.write("temperature", input.parse("TE").as(String.class));
            output.write("windDirection", input.parse("WR").as(String.class));
            output.write("windSpeed", input.parse("WG").as(String.class));
            return output;
        }
    }

    public static class ImportHeaterFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            Message output = new Message("Heater");
            output.write("ts", ts(input));
            output.write("energy", input.parse("ene").as(String.class));
            output.write("vol", input.parse("vol").as(Double.class));
            output.write("inTemp", input.parse("vlt").as(Double.class));
            output.write("outTemp", input.parse("rlt").as(Double.class));
            output.write("building", input.parse("bld").as(String.class));
            output.write("room", input.parse("node").as(String.class));
            output.write("roomName", input.parse("id").as(String.class));
            return output;
        }

    }
    public static class ImportPowerConsumptionFunction implements NessFunction {

        @Override
        public Message cast(Message input) {
            Message output = new Message("PowerConsumption");
            output.write("ts", ts(input));
            output.write("activePower", input.parse("Global_active_power").as(String.class));
            output.write("reactivePower", input.parse("Global_reactive_power").as(String.class));
            output.write("intensity", input.parse("Global_intensity").as(String.class));
            output.write("voltage", input.parse("Voltage").as(String.class));
            output.write("kitchenPower", input.parse("Sub_metering_1").as(String.class));
            output.write("laundryPower", input.parse("Sub_metering_2").as(String.class));
            output.write("hvacPower", input.parse("Sub_metering_3").as(String.class));
            return output;
        }

        private String ts(Message input) {
            String[] date = input.parse("date").as(String.class).split("/");
            String time = input.parse("time").as(String.class);
            return date[2] + "-" + zero(date[1]) + "-" + zero(date[0]) + "T" + time + "Z";
        }

        private String zero(String s) {
            return ("0" +s).substring(s.length()-1,s.length()+1);
        }

    }
    private static String ts(Message m) {
        return ts(m.parse("Date").as(String.class), m.parse("Time").as(String.class));
    }

    private static String ts(String date, String time) {
        String[] split = date.split("_");
        return split[2] + "-" + split[1] + "-" + split[0] + "T" + time + "Z";
    }

}
