package io.intino.ness.konos;

public class Launcher {

	public static class Pre {

		public static void main(String[] args) throws Exception {
			Main.main(new String[]{
					"graph.store=./temp/store",
					"nessie.token=xoxb-162074419812-gB5oNUwzxGWQ756TrRyu1Ii9",
					"topics.url=failover:(tcp://bus.siani.es:61616)",
					"topics.user=cesar",
					"topics.password=cesar",
					"topics.clientID=nessie",
					"topics.productionPaths=",
					"ness.rootPath=./temp/local"
			});
		}
	}

}