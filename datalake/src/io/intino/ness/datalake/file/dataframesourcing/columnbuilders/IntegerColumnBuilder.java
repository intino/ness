package io.intino.ness.datalake.file.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.file.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IntegerColumnBuilder extends DataFrame.ColumnBuilder {
	public IntegerColumnBuilder(File columnFile) throws IOException {
		super(columnFile);
	}

	@Override
	public boolean put(String value) throws IOException {
		os.write(ByteBuffer.allocate(4).putInt(Integer.parseInt(value)).array());
		return true;
	}
}
