package io.intino.ness.datalake.file.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.file.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DoubleColumnBuilder extends DataFrame.ColumnBuilder {
	public DoubleColumnBuilder(File columnFile) throws IOException {
		super(columnFile);
	}

	@Override
	public boolean put(String value) throws IOException {
		os.write(ByteBuffer.allocate(8).putDouble(Double.parseDouble(value)).array());
		return true;
	}
}