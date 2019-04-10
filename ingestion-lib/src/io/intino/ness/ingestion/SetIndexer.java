package io.intino.ness.ingestion;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.mapp.MappBuilder;
import io.intino.alexandria.mapp.MappStream;
import io.intino.alexandria.zet.ZFile;
import io.intino.alexandria.zet.ZetReader;
import io.intino.ness.datalake.file.FileSetStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SetIndexer {
	private final File tank;

	public SetIndexer(File tank) {
		this.tank = tank;
	}

	public void make() {
		stream(requireNonNull(tank.listFiles(File::isDirectory))).forEach(this::processTubs);
	}

	public void processTubs(File tank) {
		for (File tub : requireNonNull(tank.listFiles(File::isDirectory)))
			if (!new File(tub, FileSetStore.IndexFileName).exists()) makeIndex(tub);
	}

	private void makeIndex(File tub) {
		File destinationFile = new File(tub, FileSetStore.IndexFileName);
		List<Zet> zets = streamOf(tub);
		MappBuilder builder = new MappBuilder(zets.stream().map(z -> z.name).collect(toList()));
		builder.put(MappStream.Merge.of(zets.stream().map(this::mappStream).collect(toList())));
		try {
			builder.save(destinationFile);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private List<Zet> streamOf(File directory) {
		return zetInfos(directory).stream()
				.sorted(Comparator.comparing(s -> s.size))
				.collect(toList());
	}

	private List<Zet> zetInfos(File directory) {
		return stream(Objects.requireNonNull(directory.listFiles(f -> f.getName().endsWith(".zet")))).map(Zet::new).collect(Collectors.toList());
	}


	private MappStream mappStream(Zet zet) {
		return new MappStream() {
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

	private static class Zet {
		final int size;
		final String name;
		InputStream inputStream;

		public Zet(File file) {
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