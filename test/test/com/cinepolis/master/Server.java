package com.cinepolis.master;

import io.intino.master.core.Master;
import io.intino.master.core.MasterConfig;

import java.io.File;

public class Server {

    public static void main(String[] args) {

        MasterConfig config = new MasterConfig();
//        config.port(60555);
        config.dataDirectory(new File("temp/cinepolis-data"));
        config.logDirectory(new File("temp/logs/master"));
        config.instanceName("master-test");
        config.datalakeLoader(new TestDatalakeLoader());

        Master master = new Master(config);
        master.start();
    }
}
