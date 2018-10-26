package io.intino.ness.setstore.file;

import io.intino.konos.TripleStore;
import io.intino.ness.setstore.Scale;
import io.intino.ness.setstore.SetStore;
import io.intino.ness.setstore.SetStore.Tank;
import io.intino.ness.setstore.SetStore.Variable;
import io.intino.sezzet.operators.FileReader;
import io.intino.sezzet.operators.LongStream;
import io.intino.sezzet.operators.SetStream;
import io.intino.sezzet.operators.Union;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static io.intino.ness.setstore.file.FileSetStore.*;
import static io.intino.ness.setstore.file.FileSetTank.tripleStoreMap;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class FileSetTub implements Tank.Tub {
	private final File tub;
	private final Instant instant;
	private final Scale scale;

	public FileSetTub(File tub, Instant instant, Scale scale) {
		this.tub = tub;
		this.instant = instant;
		this.scale = scale;
	}

	@Override
	public Instant instant() {
		return instant;
	}

	@Override
	public List<Set> sets() {
		File[] files = tub.listFiles((f, n) -> n.endsWith(SetExt));
		return files != null ? stream(files).sorted().map(f -> new FileSet(f, triplestore())).collect(toList()) : emptyList();
	}

	@Override
	public List<Set> sets(SetStore.SetFilter filter) {
		return sets().stream().filter(filter).collect(toList());
	}

	@Override
	public Set set(String set) {
		File file = new File(tub, set + SetExt);
		return file.exists() ? new FileSet(file, triplestore()) : null;
	}

	private TripleStore triplestore() {
		String tank = tub.getParentFile().getName();
		String id = tank + scale.tag(instant);
		if (!tripleStoreMap.containsKey(id)) {
			File file = new File(tub, tank + InfoExt);
			if (!file.exists()) return null;
			tripleStoreMap.put(id, new TripleStore(file));
		}
		return tripleStoreMap.get(id);
	}

	public static class FileSet implements Set {
		private final File file;
		private final TripleStore variables;

		public FileSet(File file, TripleStore variables) {
			this.file = file;
			this.variables = variables;
		}

		public String name() {
			return file.getName().replace(SetExt, "");
		}

		@Override
		public Tank.Tub tub() {
			return null;
		}

		@Override
		public Stream<Long> content() {
			return null;
		}

		public InputStream inputStream() {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		public List<Variable> variables() {
			if (variables == null) return null;
//			variables.all()
//			List<String[]> list = variables.matches(set, variable).collect(Collectors.toList());
//			return !list.isEmpty() ? list.get(0)[2] : null;
			return Collections.emptyList();//TODO
		}

		@Override
		public void define(Variable variable) {
			variables.put(name(), variable.name(), variable.value());
			try {
				variables.save();
			} catch (IOException e) {
			}
		}


		@Override
		public void append(long... ids) {
			try {
				SetStream toWrite = file.exists() ? new Union(asList(new FileReader(file), new LongStream(ids))) : new LongStream(ids);
				File tempFile = new File(file + TempExt);
				write(toWrite, tempFile);
				Files.move(tempFile.toPath(), file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void append(InputStream stream) {
			file.getParentFile().mkdirs();
			try {
				Files.write(file.toPath(), IOUtils.readFully(stream, -1, true), APPEND, CREATE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//tripleStoreMap.remove(tank + scale.tag(instant));TODO
		}
	}

}
