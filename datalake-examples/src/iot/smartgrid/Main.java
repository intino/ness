package iot.smartgrid;

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
            .into("feeding.smartgrid.Heater.1");
        pump.plug("legacy.smartgrid.Weather")
            .with(ImportWeatherFunction.class)
            .into("feeding.smartgrid.Temperature.1");
        pump.plug("legacy.smartgrid.Weather")
            .with(ImportWindFunction.class)
            .into("feeding.smartgrid.Wind.1");
        pump.plug("legacy.smartgrid.PowerConsumption")
            .with(ImportPowerConsumptionFunction.class)
            .into("feeding.smartgrid.PowerConsumption.1");
        pump.execute().thread().join();

    }

    public static class ImportWeatherFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            //http://process.monitorering.no/measureit/files/pdf_datablad/reinhardt/reinhardt_mws9-5_manual.pdf
            Message output = new Message("Weather");
            output.write("ts", ts(input));
            output.write("temperature", input.read("TE").as(String.class));
            output.write("light", input.read("LX").as(String.class));
            output.write("radiation", input.read("SO").as(String.class));
            output.write("pressure", input.read("DR").as(String.class));
            output.write("humidity", input.read("FE").as(String.class));
            return output;
        }
    }

    public static class ImportWindFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            Message output = new Message("Wind");
            output.write("ts", ts(input));
            output.write("temperature", input.read("TE").as(String.class));
            output.write("windDirection", input.read("WR").as(String.class));
            output.write("windSpeed", input.read("WG").as(String.class));
            return output;
        }
    }

    public static class ImportHeaterFunction implements NessFunction {
        @Override
        public Message cast(Message input) {
            Message output = new Message("Heater");
            output.write("ts", ts(input));
            output.write("energy", input.read("ene").as(String.class));
            output.write("vol", input.read("vol").as(Double.class));
            output.write("inTemp", input.read("vlt").as(Double.class));
            output.write("outTemp", input.read("rlt").as(Double.class));
            output.write("building", input.read("bld").as(String.class));
            output.write("room", input.read("node").as(String.class));
            output.write("roomName", input.read("id").as(String.class));
            return output;
        }

    }
    public static class ImportPowerConsumptionFunction implements NessFunction {

        @Override
        public Message cast(Message input) {
            Message output = new Message("PowerConsumption");
            output.write("ts", ts(input));
            output.write("activePower", input.read("Global_active_power").as(String.class));
            output.write("reactivePower", input.read("Global_reactive_power").as(String.class));
            output.write("intensity", input.read("Global_intensity").as(String.class));
            output.write("voltage", input.read("Voltage").as(String.class));
            output.write("kitchenPower", input.read("Sub_metering_1").as(String.class));
            output.write("laundryPower", input.read("Sub_metering_2").as(String.class));
            output.write("hvacPower", input.read("Sub_metering_3").as(String.class));
            return output;
        }

        private String ts(Message input) {
            String[] date = input.read("date").as(String.class).split("/");
            String time = input.read("time").as(String.class);
            return date[2] + "-" + zero(date[1]) + "-" + zero(date[0]) + "T" + time + "Z";
        }

        private String zero(String s) {
            return ("0" +s).substring(s.length()-1,s.length()+1);
        }

    }
    private static String ts(Message m) {
        return ts(m.read("Date").as(String.class), m.read("Time").as(String.class));
    }

    private static String ts(String date, String time) {
        String[] split = date.split("_");
        return split[2] + "-" + split[1] + "-" + split[0] + "T" + time + "Z";
    }

}
