package io.intino.ness.datalake.dataframesourcing;

import io.intino.alexandria.zet.ZetStream;
import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.Datalake.SetStore.Index.Entry;
import io.intino.ness.datalake.dataframesourcing.columnbuilders.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class DataFrame {

	private final File root;
	private Datalake.SetStore.Set set;
	private List<Column> columns;

	public DataFrame(File root) {
		this.root = root;
	}

	public DataFrame with(Datalake.SetStore.Set set) {
		this.columns = new ArrayList<>();
		this.set = set;
		return this;
	}

	public void add(Stream<Entry> entryStream, String name, Column.Type type) throws IOException {
		columnBuilder(name, type).with(set).build(entryStream.iterator());
	}

	private ColumnBuilder columnBuilder(String name, Column.Type type) throws IOException {
		File metadataFile = new File(root, ".metadata");
		Files.write(metadataFile.toPath(), (name + ":" + type.name() + ":").getBytes(), CREATE, APPEND);
		File columnFile = new File(root, name + ".c");
		switch (type) {
			case Nominal:
				return new NominalColumnBuilder(columnFile, metadataFile);
			case Float:
				return new FloatColumnBuilder(columnFile);
			case Double:
				return new DoubleColumnBuilder(columnFile);
			case Integer:
				return new IntegerColumnBuilder(columnFile);
			case Boolean:
				return new BooleanColumnBuilder(columnFile);
			case String:
		}
		return new StringColumnBuilder(columnFile);
	}

	public static class Column {

		private String name;
		private Type type;
		private byte[] data;

		public enum Type {
			Nominal,
			Integer,
			Float,
			Double,
			Boolean,
			String
		}

	}

	public static abstract class ColumnBuilder {
		protected final OutputStream os;
		private Datalake.SetStore.Set set;

		public ColumnBuilder(File file) throws IOException {
			this.os = new GZIPOutputStream(new FileOutputStream(file));
		}

		ColumnBuilder with(Datalake.SetStore.Set set) {
			this.set = set;
			return this;
		}

		public abstract boolean put(String value) throws IOException;

		public void close() throws IOException{
			os.flush();
			os.close();
		}

		void build(Iterator<Entry> entries) throws IOException {
			ZetStream ids = set.content();
			while (ids.hasNext()) add(ids.next(), entries);
			close();
		}

		private boolean add(long id, Iterator<Entry> entries) throws IOException {
			while (entries.hasNext()) {
				Entry entry = entries.next();
				if (entry.id >= id) return put(entry.id > id ? null : entry.set);
			}
			return put(null);
		}
	}
}
