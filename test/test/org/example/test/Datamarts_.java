package org.example.test;

import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;

public class Datamarts_ {

	public static void main(String[] args) {

		TrooperTerminal terminal = null;

		MasterDatamart datamart = terminal.masterDatamart();

		var timelines = datamart.timelines("12345");

		var reels = datamart.reels("12345");
	}
}
