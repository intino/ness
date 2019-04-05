package io.intino.ness.datalake.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BooleanColumnBuilder extends DataFrame.ColumnBuilder {
	public BooleanColumnBuilder(File columnFile) throws IOException {
		super(columnFile);
	}

	@Override
	public boolean put(String value) throws IOException {
		os.write(ByteBuffer.allocate(1).put((byte) (Boolean.parseBoolean(value) ? 1 : 0)).array());
		return true;
	}
}
