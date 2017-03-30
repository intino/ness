package io.intino.ness.datalake.konos;

public class Launcher {

	public static class Pre {

		public static void main(String[] args) throws Exception {
			Main.main(new String[]{
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