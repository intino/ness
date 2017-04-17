package io.smartgrid;

import io.intino.ness.datalake.FilePumpingStation;
import io.intino.ness.datalake.NessPumpingStation;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFunction;

import static io.intino.ness.datalake.Joints.sortingBy;

public class Main {

    public static void main(String[] args) throws Exception {
        NessPumpingStation station = new FilePumpingStation("datalake-examples/local.store");

        station.pipe("legacy.smartgrid.Heater").join(sortingBy("Time"));

        station.pipe("legacy.smartgrid.Heater")
                .with(ImportHeaterFunction.class)
                .to("channel.smartgrid.Heater.1");
        station.pipe("legacy.smartgrid.Weather")
                .with(ImportWeatherFunction.class)
                .to("channel.smartgrid.Temperature.1");
        station.pipe("legacy.smartgrid.PowerConsumption")
                .with(ImportPowerConsumptionFunction.class)
                .to("channel.smartgrid.PowerConsumption.1");
        Thread t1 = station.pump("legacy.smartgrid.Heater").thread();
        Thread t2 = station.pump("legacy.smartgrid.Weather").thread();
        Thread t3 = station.pump("legacy.smartgrid.PowerConsumption").thread();

        t1.join();
        t2.join();
        t3.join();

    }

    public static class ImportWeatherFunction implements MessageFunction {
        @Override
        public Message cast(Message input) {
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

    public static class ImportHeaterFunction implements MessageFunction {
        @Override
        public Message cast(Message input) {
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
    public static class ImportPowerConsumptionFunction implements MessageFunction {

        @Override
        public Message cast(Message input) {
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
