package io.intino.ness.datalake.file.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.file.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;

public class StringColumnBuilder extends DataFrame.ColumnBuilder {

	public StringColumnBuilder(File file) throws IOException {
		super(file);
	}

	@Override
	public boolean put(String value) throws IOException {
		os.write(value.getBytes());
		return true;
	}

	@Override
	public void close() throws IOException {
		os.flush();
		os.close();
	}
}