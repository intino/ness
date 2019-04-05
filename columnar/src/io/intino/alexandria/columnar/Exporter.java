package io.intino.alexandria.columnar;

import java.io.File;
import java.io.IOException;

public interface Exporter {

	void export(File file) throws IOException;
}
