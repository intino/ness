package io.intino.master.io;

import io.intino.master.model.Triple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class TriplesFileWriter implements AutoCloseable {

	private final File stage;
	private final String sender;
	private BufferedWriter writer;

	public TriplesFileWriter(File stage, String sender) throws IOException {
		this.stage = stage;
		this.sender = sender;
		this.writer = new BufferedWriter(new FileWriter(file(), true));
	}

	public void write(Triple triple) throws IOException {
		writer.write(triple.toString());
		writer.newLine();
	}

	private File file() {
		File file = new File(stage, today() + "/" + sender + ".triples");
		file.getParentFile().mkdirs();
		return file;
	}

	private String today() {
		return LocalDate.now().toString().replace("-", "");
	}

	@Override
	public void close() throws IOException {
		if(writer == null) return;
		writer.close();
		writer = null;
	}
}
