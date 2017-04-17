package io.fracfocus;

import io.intino.ness.datalake.FilePumpingStation;
import io.intino.ness.datalake.NessPumpingStation;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageFunction;

public class Main {

    public static void main(String[] args) throws Exception {
        NessPumpingStation station = new FilePumpingStation("datalake-examples/local.store");
        station.pipe("legacy.frac.Job")
            .with(ImportFracJobFunction.class)
            .to("channel.frac.Job.1");
        station.pump("legacy.frac.Job").thread().join();
    }

    public static class ImportFracJobFunction implements MessageFunction {

        @Override
        public Message cast(Message input) {
            String apiCode = read(input, "APINumber");
            Message output = new Message("Well");
            output.ts(ts(input));
            output.write("code", apiCode);
            output.write("name", read(input, "WellName"));
            output.write("state", read(input, "state"));
            output.write("stateName", read(input, "stateName"));
            output.write("county", read(input, "county"));
            output.write("countyName", read(input, "countyName"));
            output.write("latitude", read(input, "latitude"));
            output.write("longitude", read(input, "longitude"));
            output.write("projection", read(input, "projection"));
            output.write("tvd", read(input, "tvd"));
            output.write("isFederal", read(input, "FederalWell"));
            output.write("isIndian", read(input, "IndianWell"));
            output.write("operator", read(input, "OperatorName"));
            output.write("waterVolume", read(input, "TotalBaseWaterVolume"));
            output.write("nonWaterVolume", read(input, "TotalBaseNonWaterVolume"));
            return output;
        }

        private String read(Message input, String attribute) {
            return check(input.parse(attribute).as(String.class));
        }

        private String check(String value) {
            return "NULL".equals(value) ? null : value;
        }

        private String ts(Message input) {
            String date = read(input, "JobStartDate");
            return date.substring(0,10) + "T" + date.substring(11,19) + "Z";
        }

    }
}
