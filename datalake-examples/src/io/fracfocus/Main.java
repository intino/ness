package io.fracfocus;

import io.intino.ness.datalake.NessDataLake;
import io.intino.ness.datalake.NessFunction;
import io.intino.ness.datalake.NessPump;
import io.intino.ness.datalake.FileDataLake;
import io.intino.ness.inl.Message;

public class Main {

    public static void main(String[] args) throws Exception {
        NessDataLake dataLake = new FileDataLake("datalake-examples/local.store");
        NessPump pump = new NessPump(dataLake);
        pump.plug("legacy.frac.usa.Job")
            .with(ImportFracJobFunction.class)
            .into("feeding.frac.usa.Job.1");
        pump.plug("legacy.frac.usa.Job")
            .with(ImportFracJobFunction.class)
            .into("feeding.frac.usa.Job.1");
        pump.execute().thread().join();
    }

    public static class ImportFracJobFunction implements NessFunction {

        @Override
        public Message cast(Message input) {
            String apiCode = read(input, "APINumber");
            Message output = new Message("Well");
            output.write("ts",ts(input));
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
