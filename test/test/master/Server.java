package master;

import io.intino.datahub.box.DataHubBox;

public class Server {

    public static void main(String[] args) {

        DataHubBox box = new DataHubBox(new String[]{
                "home=temp",
                "backup_directory=temp/datahub",
                "broker_port=60001",
                "broker_secondary_port=temp/datahub",
                "ui_port=60023",
                "master_instance_name=test-datahub",
                "master_port=5701",
                "master_host=localhost",
                "master_serializer=tsv",
        });

//        NessGraph graph = new Graph().as(NessGraph.class);
//        graph.core$().loadStashes();
//
//        box.put();
//
//        box.start();
    }
}
