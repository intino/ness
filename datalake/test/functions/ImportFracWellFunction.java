package functions;

import io.intino.ness.datalake.NessFunction;
import io.intino.ness.inl.Message;

public class ImportFracWellFunction implements NessFunction {

    @Override
    public Message cast(Message input) {
        String apiCode = input.read("APINumber").as(String.class);
        Message output = new Message("Well");
        output.write("ts",ts(input));
        output.write("code", apiCode);
        output.write("name",input.read("WellName").as(String.class));
        output.write("state",input.read("state").as(String.class));
        output.write("stateName",input.read("stateName").as(String.class));
        output.write("county",input.read("county").as(String.class));
        output.write("countyName",input.read("countyName").as(String.class));
        output.write("latitude",input.read("latitude").as(String.class));
        output.write("longitude",input.read("longitude").as(String.class));
        output.write("projection",input.read("projection").as(String.class));
        output.write("tvd",input.read("tvd").as(String.class));
        output.write("isFederal",input.read("FederalWell").as(String.class));
        output.write("isIndian",input.read("IndianWell").as(String.class));
        output.write("operator", input.read("OperatorName").as(String.class));
        output.write("waterVolume", input.read("TotalBaseWaterVolume").as(String.class));
        output.write("nonWaterVolume", input.read("TotalBaseNonWaterVolume").as(String.class));
        return output;
    }

    private String ts(Message input) {
        String date = input.read("JobStartDate").as(String.class);
        return date.substring(0,10) + "T" + date.substring(11,19) + "Z";
    }

}
