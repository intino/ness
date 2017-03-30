package functions;

import io.intino.ness.datalake.NessFunction;
import io.intino.ness.inl.Message;

public class ImportPowerConsumptionFunction implements NessFunction {

    @Override
    public Message cast(Message input) {
        Message output = new Message("PowerConsumption", "feed.edf.PowerConsumption.1");
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
