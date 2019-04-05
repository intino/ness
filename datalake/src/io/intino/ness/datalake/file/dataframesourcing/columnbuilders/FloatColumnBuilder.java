package io.intino.ness.datalake.file.dataframesourcing.columnbuilders;

import io.intino.ness.datalake.file.dataframesourcing.DataFrame;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FloatColumnBuilder extends DataFrame.ColumnBuilder {
	public FloatColumnBuilder(File file) throws IOException {
		super(file);
	}

	@Override
	public boolean put(String value) throws IOException {
		os.write(ByteBuffer.allocate(4).putFloat(Float.parseFloat(value)).array());
		return true;
	}
}
