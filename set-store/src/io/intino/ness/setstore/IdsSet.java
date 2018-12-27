package io.intino.ness.setstore;

import io.intino.sezzet.operators.SetStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdsSet {

	private long[] ids;

	public IdsSet(SetStream stream) {
		List<Long> longList = new ArrayList<>();
		while (stream.hasNext()) longList.add(stream.next());
		ids = ids(longList);
	}

	public long[] ids() {
		return ids;
	}

	private static long[] ids(List<Long> longList) {
		long[] longs = new long[longList.size()];
		for (int i = 0; i < longList.size(); i++) longs[i] = longList.get(i);
		return longs;
	}


	public boolean isIn(long id) {
		return Arrays.binarySearch(ids, id) >= 0;
	}
}
