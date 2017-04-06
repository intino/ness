package iot.sensors;

import io.intino.ness.datalake.*;

/**
 * Created by JJ on 05/04/2017.
 */
public class Main {
    public static void main(String[] args) {
        NessDataLake dataLake = new FileDataLake("datalake-examples/local.store");
        NessFeeder feeder = new NessFeeder(dataLake.get("feeding.happysense.Dialog"));
        feeder.close();
    }
}
