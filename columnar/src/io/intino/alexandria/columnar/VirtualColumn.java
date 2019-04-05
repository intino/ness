package io.intino.alexandria.columnar;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.assa.AssaStream;

public class VirtualColumn extends Column {

	private final AssaStreamProvider provider;

	public VirtualColumn(String name, ColumnType type, AssaStreamProvider provider) {
		super(name, type);
		this.provider = provider;
	}


	public VirtualColumn(String name, ColumnType type, Mapper mapper, AssaStreamProvider provider) {
		super(name, type, mapper);
		this.provider = provider;
	}

	public AssaStream streamOf(Timetag timetag) {
		return provider.streamOf(timetag);
	}

	public interface AssaStreamProvider {
		AssaStream streamOf(Timetag timetag);
	}
}
