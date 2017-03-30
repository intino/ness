package io.intino.ness.konos;

public class Launcher {

	public static class Pre {

		public static void main(String[] args) throws Exception {
			Main.main(new String[]{
					"graph.store=./temp/store",
					"nessy.token=xoxb-162074419812-gB5oNUwzxGWQ756TrRyu1Ii9",
					"ness.url=",
					"ness.user=",
					"ness.password=",
					"ness.clientID=",
					"ness.productionPaths=",
					"ness.rootPath=./temp/local"
			});
		}
	}

}