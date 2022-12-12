package master;

import io.intino.ness.master.data.FileEntityLoader;
import io.intino.ness.master.data.MasterTripletsDigester;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializers;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;

// Converts a repository of triplets into a proper triplets datalake
// It will only create raster tubs
public class RepositoryToDatalakeConverter {

	public static void main(String[] args) throws Exception {

		File repository = new File("temp/cinepolis-data/datasets");
		File datalake = new File("temp/datalake");

		MasterTripletsDigester digester = MasterTripletsDigester.createDefault();

		MasterTripletsDigester.Result digestion = digester.load(new FileEntityLoader(repository), MasterSerializers.getDefault());

		System.out.println(digestion.stats());

		createDatalake(digestion, datalake);

		System.out.println("Triplets datalake created");
	}

	private static void createDatalake(MasterTripletsDigester.Result digestion, File datalake) {
		File root = new File(datalake, "triplets");
		root.mkdirs();

		for(var entry : digestion.records().values().stream().collect(Collectors.groupingBy(TripletRecord::type)).entrySet()) {
			File tank = new File(root, StringUtils.capitalize(entry.getKey()));
			tank.mkdirs();
			File tub = new File(tank, "00000000.triplets");
			serialize(entry.getValue(), tub);
			System.out.println(tub + " done");
		}
	}

	private static void serialize(List<TripletRecord> records, File tub) {
		try {
			Files.write(tub.toPath(), records.stream().map(TripletRecord::toString).collect(Collectors.toList()), CREATE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
