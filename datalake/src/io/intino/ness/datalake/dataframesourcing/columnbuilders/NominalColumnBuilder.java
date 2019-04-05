package io.intino.ness.datalake.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class NominalColumnBuilder extends DataFrame.ColumnBuilder {
	private final File metadataFile;
	private final List<String> values;

	public NominalColumnBuilder(File columnFile, File metadataFile) throws IOException {
		super(columnFile);
		this.metadataFile = metadataFile;
		this.values = new ArrayList<>();

	}

	@Override
	public boolean put(String value) throws IOException {
		if (values.contains(value)) os.write(ByteBuffer.allocate(4).putInt(values.indexOf(value) + 1).array());
		else {
			values.add(value);
			os.write(values.size());
		}
		return true;
	}

	@Override
	public void close() throws IOException {
		Files.write(metadataFile.toPath(), join(",", values).getBytes(), CREATE, APPEND);
		os.flush();
		os.close();
	}
}
