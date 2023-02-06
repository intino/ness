//package master.general;
//
//import io.intino.alexandria.terminal.JmsConnector;
//import io.intino.test.datahubtest.TestTerminal;
//import io.intino.test.datahubtest.master.Entities;
//import io.intino.test.datahubtest.master.entities.Zone;
//import io.intino.test.datahubtest.master.structs.GeoPoint;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class MasterClient_ {
//
//	public static void main(String[] args) {
//		TestTerminal terminal = createTerminal();
//		Entities entities = terminal.entities();
//		entities.publish(new Zone("a9d7d8a8-ef30-46a5-888d-45843a68dea3:zone")
//				.ownerZone(null)
//				.children(Collections.emptyList())
//				.place(place())
//				.name("A B C")
//		);
//		entities.disableZone("a9d7d8a8-ef30-46a5-888d-45843a68dea3");
//
//		entities.addApplicationModuleEntityListener(event -> {
//			System.out.println(event.type() + " => " + event.entityId());
//		});
//	}
//
//	private static List<GeoPoint> place() {
//		return toGeoPoints(List.of(28.1326967,-15.4283801,28.132718,-15.4284418,28.132744,-15.4285008,28.1327487,-15.4285652,28.1327605,-15.4286135,28.1327558,-15.4286537,28.132673,-15.4286591,28.1326257,-15.4286859,28.13255,-15.4287583,28.132498,-15.4288066,28.1324531,-15.4288495,28.132427,-15.4288897,28.1323868,-15.4289112,28.1323703,-15.42893,28.1323703,-15.4289809,28.1323821,-15.4290453,28.132401,-15.4290909,28.1324105,-15.4291338,28.1324128,-15.429166,28.1324223,-15.4291901,28.1324472,-15.4292451,28.1325182,-15.4292237,28.1325288,-15.429221,28.1325536,-15.4292116,28.1325737,-15.4292009,28.1326565,-15.4292599,28.1326127,-15.4293176,28.1325902,-15.4293483,28.1325637,-15.4293906,28.132543,-15.4294208,28.1324945,-15.4294758,28.1324425,-15.429528,28.1324029,-15.4295455,28.1323881,-15.4295522,28.1323798,-15.4295496,28.1323549,-15.4295656,28.1323266,-15.4295884,28.1322757,-15.4295924,28.1322236,-15.4295468,28.1321515,-15.4296059,28.132148,-15.4296474,28.1320865,-15.4296595,28.132006,-15.4296635,28.131915,-15.4296635,28.1318405,-15.4296582,28.1317867,-15.4296609,28.1317518,-15.4296649,28.1317068,-15.4296636,28.1316796,-15.4296608,28.13165,-15.4296541,28.131585,-15.4296474,28.1315543,-15.4296501,28.1315179,-15.4296444,28.1314863,-15.4296481,28.1314526,-15.4296502,28.1313697,-15.4296421,28.1313697,-15.4294691,28.131365,-15.429402,28.1313674,-15.4293028,28.1313674,-15.4291982,28.1313674,-15.4291284,28.1313674,-15.4290855,28.131365,-15.4290104,28.131365,-15.4289621,28.1313603,-15.4288844,28.1313674,-15.4288254,28.1314502,-15.4288173,28.1315424,-15.4288173,28.1316394,-15.4288173,28.1317245,-15.4288093,28.1317718,-15.4287959,28.131831,-15.4287851,28.1319209,-15.4287637,28.1320131,-15.4287422,28.1321054,-15.4287127,28.1321716,-15.4286912,28.1322922,-15.4286617,28.1323561,-15.4286456,28.1323916,-15.4286161,28.132427,-15.4285947,28.1324838,-15.4285759,28.1325737,-15.4285223,28.1326092,-15.428482,28.1326494,-15.4284659,28.1326659,-15.4284257,28.1326967,-15.4283801));
//	}
//
//	private static List<GeoPoint> toGeoPoints(List<Double> params) {
//		List<GeoPoint> points = new ArrayList<>();
//		for(int i = 0;i < params.size();i+=2) points.add(new GeoPoint(params.get(i), params.get(i + 1)));
//		return points;
//	}
//
//	private static TestTerminal createTerminal() {
//		JmsConnector connector = new JmsConnector(
//				"failover:(tcp://localhost:62123)",
//				"test",
//				"test",
//				"test",
//				new File("temp/cache")
//		);
//		connector.start();
//		return new TestTerminal(connector);
//	}
//}
