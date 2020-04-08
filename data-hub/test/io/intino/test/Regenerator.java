package io.intino.test;

import io.intino.datahub.datalake.regenerator.Mapper;
import io.intino.datahub.datalake.regenerator.MapperLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class Regenerator {


	public static final String MAPPER_CODE =
			"package io.provista.datahub.box.regenerator.mappers;\n" +
					"import io.intino.alexandria.Timetag;\n" +
					"import io.intino.alexandria.datalake.Datalake;\n" +
					"import io.intino.alexandria.event.Event;\n" +
					"import io.intino.datahub.datalake.regenerator.Mapper;\n" +
					"\n" +
					"import java.util.Arrays;\n" +
					"import java.util.List;\n" +
					"\n" +
					"public class Bertha implements Mapper {\n" +
					"\tprivate static List<String> cuentas = Arrays.asList(\"MH95\", \"MA73\", \"MZZ9\", \"MA81\", \"M541\", \"M110\", \"MZZ6\", \"MZZB\", \"MB41\",\n" +
					"\t\t\t\"M278\", \"MZ28\", \"M097\", \"MZZ8\", \"MZZD\", \"M438\", \"M581\", \"MXXX\", \"M010\", \"M188\", \"M389\", \"M386\", \"MA93\", \"MA33\", \"MZZJ\",\n" +
					"\t\t\t\"M391\", \"MA80\", \"M198\", \"M482\", \"M287\", \"M415\", \"MF61\", \"MZZK\", \"M641\", \"M484\", \"M199\", \"MA57\", \"M677\", \"M189\", \"MC42\",\n" +
					"\t\t\t\"MG66\", \"MZZE\", \"MK16\", \"M444\", \"M282\", \"M459\", \"MF62\", \"M412\", \"M350\", \"MA66\", \"MA91\", \"MC38\", \"MJ26\", \"MZZG\", \"MZZP\",\n" +
					"\t\t\t\"MA54\", \"MZZ7\", \"MB42\", \"M0Z1\", \"M226\", \"M473\", \"M050\", \"M257\", \"M521\", \"M989\", \"M351\", \"M716\", \"M458\", \"MK48\", \"M302\",\n" +
					"\t\t\t\"MZZC\", \"MZZM\", \"MA38\", \"M277\", \"M410\", \"M540\", \"MZZH\", \"MB93\", \"MZZQ\", \"MZZL\", \"MZZF\", \"M264\", \"M437\", \"M361\", \"MZZN\",\n" +
					"\t\t\t\"M755\", \"M210\", \"M075\", \"M526\", \"MC37\", \"M155\", \"MC24\", \"M049\", \"MZZR\", \"MD44\", \"M542\", \"MA67\", \"M436\", \"M256\", \"MA87\",\n" +
					"\t\t\t\"M449\", \"MB66\", \"M527\", \"MZZ0\", \"MB68\", \"MZZA\");\n" +
					"\n" +
					"\tprivate static final Filter FILTER = new Filter() {\n" +
					"\t\t@Override\n" +
					"\t\tpublic boolean allow(Event event) {\n" +
					"\t\t\treturn cuentas.contains(event.toMessage().get(\"cuenta\").toString());\n" +
					"\t\t}\n" +
					"\n" +
					"\t\t@Override\n" +
					"\t\tpublic boolean allow(Datalake.EventStore.Tank tank) {\n" +
					"\t\t\treturn true;\n" +
					"\t\t}\n" +
					"\n" +
					"\t\t@Override\n" +
					"\t\tpublic boolean allow(Datalake.EventStore.Tank tank, Timetag timetag) {\n" +
					"\t\t\treturn true;\n" +
					"\t\t}\n" +
					"\t};\n" +
					"\n" +
					"\t@Override\n" +
					"\tpublic Event apply(Event event) {\n" +
					"\t\treturn null;\n" +
					"\t}\n" +
					"\n" +
					"\t@Override\n" +
					"\tpublic Filter filter() {\n" +
					"\t\treturn FILTER;\n" +
					"\t}\n" +
					"\n" +
					"\t@Override\n" +
					"\tpublic String description() {\n" +
					"\t\treturn \"Eliminar cuentas pertenecientes a una blacklist. Issue 92\";\n" +
					"\t}\n" +
					"}\n";

	@Test
	public void compileAndLoad() {
		MapperLoader loader = new MapperLoader(new File("./temp"));
		Mapper mapper = loader.compileAndLoad(MAPPER_CODE);
		Assert.assertNotNull(mapper);
	}
}
