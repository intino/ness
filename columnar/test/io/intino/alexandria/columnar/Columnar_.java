package io.intino.alexandria.columnar;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.assa.AssaStream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static io.intino.alexandria.columnar.Column.ColumnType.nominal;
import static io.intino.alexandria.columnar.Column.ColumnType.string;
import static java.util.Arrays.asList;

public class Columnar_ {
	private Columnar columnar;
	private File exportsFolder;
	private File columnarFolder;

	@Before
	public void setUp() {
		columnarFolder = new File("temp/columnar");
		columnarFolder.mkdirs();
		columnar = new Columnar(columnarFolder);
		exportsFolder = new File("temp/exports");
		exportsFolder.mkdirs();
	}

	@Test
	public void should_load_assa_from_one_zet() {
		columnar.importColumn(new File("test-res/zets/Activos"));
	}

	@Test
	public void should_load_assa_from_multiple_zets() {
		columnar.importColumn(new File("test-res/zets/Zona"));
	}

	@Test
	public void should_export_column_to_csv() throws IOException {
		should_load_assa_from_one_zet();
		columnar.select(new Column("Activos", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Activos_201812.csv"));
	}

	@Test
	public void should_export_column_to_arff() throws IOException {
		should_load_assa_from_one_zet();
		columnar.select(new Column("Activos", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoARFF(new File(exportsFolder, "Alta_201812.arff"));
	}

	@Test
	public void should_export_multivalued_column_zona_to_csv() throws IOException {
		should_load_assa_from_multiple_zets();
		columnar.select(new Column("Zona", string(), v -> String.join(",", v)))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Zona_201812.csv"));
	}

	@Test
	public void should_export_composite_column_zona_and_activos_to_csv() throws IOException {
		should_load_assa_from_one_zet();
		should_load_assa_from_multiple_zets();
		columnar.select(new CompositeColumn("mixed", asList(
				new Column("Zona", string()),
				new Column("Activos", string())), l -> l.get(new Random().nextInt(l.size()))))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Zona_201812.csv"));
	}

	@Test
	public void should_export_multivalued_column_to_arff() throws IOException {
		should_load_assa_from_multiple_zets();
		columnar.select(new Column("Zona", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoARFF(new File(exportsFolder, "Zona_201812.arff"));
	}

	@Test
	public void should_export_multiple_columns_to_csv() throws IOException {
		should_load_assa_from_one_zet();
		should_load_assa_from_multiple_zets();
		columnar.select(new Column("Activos", nominal(new String[]{"true", "false"}), l -> !l.isEmpty()), new Column("Zona", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Mix_201812.csv"));
	}

	@Test
	public void should_export_multiple_columns_filtered_to_csv() throws IOException {
		should_load_assa_from_one_zet();
		should_load_assa_from_multiple_zets();
		columnar.select(new Column("Activos", nominal(new String[]{"true", "false"}), l -> !l.isEmpty()), new Column("Zona", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Mix_201812.csv"), r -> r.id() == 1000100257L);
	}

	@Test
	public void should_export_virtual_columns_to_csv() throws IOException {
		should_load_assa_from_one_zet();
		should_load_assa_from_multiple_zets();
		columnar.select(new Column("Activos", nominal(new String[]{"true", "false"}), l -> !l.isEmpty()), new Column("Zona", string()), new VirtualColumn("virtual", string(), provider()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Mix_201812.csv"));
	}

	private VirtualColumn.AssaStreamProvider provider() {
		return timetag -> new AssaStream() {

			private boolean sent;
			private Item item = new Item() {
				@Override
				public long key() {
					return 1000100257L;
				}

				@Override
				public List<String> value() {
					return Collections.singletonList("test");
				}
			};

			@Override
			public Item next() {
				sent = true;
				return item;
			}

			@Override
			public boolean hasNext() {
				return !sent;
			}

			@Override
			public void close() {

			}
		};
	}

	@Test
	public void should_export_filtered_column_to_csv() throws IOException {
		columnar.select(new Column("Activos", nominal(new String[]{"true", "false"})), new Column("Zona", string()))
				.from(new Timetag("201812"))
				.to(new Timetag("201812"))
				.intoCSV(new File(exportsFolder, "Mix_filtered_201812.csv"), r -> r.id() == 1000100257L);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(columnarFolder);
//		FileUtils.deleteDirectory(exportsFolder);
	}
}
