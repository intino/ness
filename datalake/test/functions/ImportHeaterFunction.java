package functions;

import io.intino.ness.datalake.NessFunction;
import io.intino.ness.inl.Message;

public class ImportHeaterFunction implements NessFunction {

    @Override
    public Message cast(Message input) {
        Message output = new Message("Heater");
        output.write("ts", ts(input));
        output.write("energy", input.read("ene").as(String.class));
        output.write("vol", input.read("vol").as(Double.class));
        output.write("inTemp", input.read("vlt").as(Double.class));
        output.write("outTemp", input.read("rlt").as(Double.class));
        output.write("building", input.read("bldg").as(String.class));
        output.write("room", input.read("node").as(String.class));
        output.write("roomName", input.read("beschreibung").as(String.class));
        return output;
    }

    private String ts(Message m) {
        return ts(m.read("Datum").as(String.class), m.read("Uhrzeit").as(String.class));
    }

    private String ts(String date, String time) {
        String[] split = date.split("_");
        return split[2]+"-"+split[1]+"-"+split[0]+"T"+time+"Z";
    }


}
