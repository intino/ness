package io.intino.ness.box;

public class PlatformTest {

	public static void main(String[] args) {
		Main.main(new String[]{"broker_port=63000", "mqtt_port=1884", "workspace=./temp/ness/", "connector_id=id", "slackToken=", "scale=Day", "configurationModel=ConfigurationTest"});
	}
}
