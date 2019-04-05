package io.intino.alexandria.columnar;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.assa.AssaBuilder;
import io.intino.alexandria.assa.AssaReader;
import io.intino.alexandria.assa.AssaStream;
import io.intino.alexandria.assa.AssaStream.Merge;
import io.intino.alexandria.columnar.exporters.ARFFExporter;
import io.intino.alexandria.columnar.exporters.CSVExporter;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zet.ZFile;
import io.intino.alexandria.zet.ZetReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Columnar {
	private static final String ASSA_FILE = ".assa";
	private final File root;

	public Columnar(File root) {
		this.root = root;
	}

	public String[] columns() {
		return root.list((f, n) -> f.isDirectory());
	}

	public Select select(Column... columns) {
		return new Select() {
			private Timetag from;

			@Override
			public Select from(Timetag timetag) {
				this.from = timetag;
				return this;
			}

			@Override
			public Into to(Timetag to) throws IOException {
				Map<Timetag, List<AssaStream>> assas = new LinkedHashMap<>();
				for (Timetag timetag : from.iterateTo(to)) {
					assas.put(timetag, new ArrayList<>());
					for (Column column : columns) {
						if (column instanceof VirtualColumn) assas.get(timetag).add(((VirtualColumn) column).streamOf(timetag));
						else if (column instanceof CompositeColumn)
							assas.get(timetag).add(compositeStream((CompositeColumn) column, timetag));
						else assas.get(timetag).add(new AssaReader(assaFile(column.name(), new File(root, timetag.value()).getName())));
					}
				}
				return into(Arrays.asList(columns), assas);
			}
		};
	}

	private AssaStream compositeStream(CompositeColumn composite, Timetag timetag) throws IOException {
		List<AssaStream> streams = new ArrayList<>();
		for (Column c : composite.columns())
			streams.add(new AssaReader(assaFile(c.name(), new File(root, timetag.value()).getName())));
		return Merge.of(streams);
	}

	public void importColumn(File file) {
		stream(directoriesIn(file)).parallel().forEach(this::build);
	}

	private void build(File source) {
		try {
			File destinationFile = destinationOf(source);
			if (destinationFile.exists()) return;
			List<ZetInfo> zets = streamOf(source);
			AssaBuilder builder = new AssaBuilder(zets.stream().map(z -> z.name).collect(toList()));
			builder.put(Merge.of(zets.stream().map(this::assaStream).collect(toList())));
			builder.save(destinationFile);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File destinationOf(File source) {
		String timetag = source.getName();
		String tank = source.getParentFile().getName();
		File file = new File(root, tank + "/" + timetag + ASSA_FILE);
		file.getParentFile().mkdirs();
		return file;
	}

	private Select.Into into(List<Column> columns, Map<Timetag, List<AssaStream>> assas) {
		return new Select.Into() {
			@Override
			public Iterator<Row> toIterator() {
				return new Iterator<Row>() {
					Timetag current = assas.keySet().iterator().next();
					RowIterator currentIterator = new RowIterator(current, assas.get(current), columns);

					@Override
					public boolean hasNext() {
						if (currentIterator.hasNext()) return true;
						current = current.next();
						if (!assas.containsKey(current)) return false;
						currentIterator = new RowIterator(current, assas.get(current), columns);
						return currentIterator.hasNext();
					}

					@Override
					public Row next() {
						return currentIterator.next();
					}
				};
			}

			@Override
			public void intoCSV(File file) throws IOException {
				new CSVExporter(toIterator(), columns, r -> true).export(file);
			}

			@Override
			public void intoCSV(File file, Select.RowFilter filter) throws IOException {
				new CSVExporter(toIterator(), columns, filter).export(file);
			}

			@Override
			public void intoARFF(File file) throws IOException {
				new ARFFExporter(toIterator(), columns, r -> true).export(file);
			}

			@Override
			public void intoARFF(File file, Select.RowFilter filter) throws IOException {
				new ARFFExporter(toIterator(), columns, filter).export(file);
			}
		};
	}

	private List<ZetInfo> streamOf(File directory) {
		return zetInfos(directory).stream()
				.sorted(Comparator.comparing(s -> s.size))
				.collect(toList());
	}

	private AssaStream assaStream(ZetInfo zet) {
		return new AssaStream() {
			ZetReader reader = new ZetReader(zet.inputStream());

			@Override
			public Item next() {
				long key = reader.next();
				return new Item() {
					@Override
					public long key() {
						return key;
					}

					@Override
					public List<String> value() {
						return singletonList(zet.name);
					}
				};
			}

			@Override
			public boolean hasNext() {
				return reader.hasNext();
			}

			@Override
			public void close() {
			}
		};
	}

	private List<ZetInfo> zetInfos(File directory) {
		return stream(Objects.requireNonNull(directory.listFiles(f -> f.getName().endsWith(".zet")))).map(ZetInfo::new).collect(Collectors.toList());
	}

	private File[] directoriesIn(File directory) {
		return Objects.requireNonNull(directory.listFiles(File::isDirectory));
	}


	private File assaFile(String column, String name) {
		return new File(columnDirectory(column), name + ASSA_FILE);
	}


	private File columnDirectory(String column) {
		File file = new File(root, column);
		file.mkdirs();
		return file;
	}

	public interface Select {

		Select from(Timetag timetag) throws IOException;

		Into to(Timetag timetag) throws IOException;


		interface RowFilter extends Predicate<Row> {
		}

		interface Into {

			Iterator<Row> toIterator();

			void intoCSV(File file) throws IOException;

			void intoCSV(File file, Select.RowFilter filter) throws IOException;

			void intoARFF(File file) throws IOException;

			void intoARFF(File file, Select.RowFilter filter) throws IOException;
		}
	}

	private static class ZetInfo {

		final int size;
		final String name;
		InputStream inputStream;

		public ZetInfo(File file) {
			this.name = nameOf(file);
			this.size = sizeOf(file);
			try {
				inputStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		InputStream inputStream() {
			return inputStream;
		}

		private int sizeOf(File file) {
			try {
				return (int) new ZFile(file).size();
			} catch (IOException e) {
				Logger.error(e);
				return 0;
			}
		}

		private String nameOf(File file) {
			return file.getName().substring(0, file.getName().lastIndexOf('.'));
		}
	}

}
